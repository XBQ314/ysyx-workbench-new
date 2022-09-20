import chisel3._
import chisel3.util._

class CLINT extends Module
{
    val io = IO(new Bundle
    {
        val inst = Input(UInt(32.W))
        val IFpc = Input(UInt(64.W))
        val IDpc = Input(UInt(64.W))

        val global_int_en = Input(Bool())
        val int_flag = Input(Bool())

        val mstatus_in = Input(UInt(64.W))
        val mepc_in = Input(UInt(64.W))
        val mtvec_in = Input(UInt(64.W))
        val mcause_in = Input(UInt(64.W))
        val mip_in = Input(UInt(64.W))
        val mie_in = Input(UInt(64.W))

        val csr_enw = Output(Bool())
        val mstatus_out = Output(UInt(64.W))
        val mepc_out = Output(UInt(64.W))
        val mcause_out = Output(UInt(64.W))
        val mip_out = Output(UInt(64.W))
        val mtimecmp_out = Output(UInt(64.W))
        val mtime_out = Output(UInt(64.W))

        val mtimecmp_enw = Input(Bool())
        val mtimecmp_in = Input(UInt(64.W))
        val mtime_enw = Input(Bool())
        val mtime_in = Input(UInt(64.W))

        // 这两个信号为了应对这样的情况: 
        // 当EXE级运行了B型跳转指令，并且算出来是跳转，这样ID级的PC就是错的, 需要被冲刷掉
        // 如果此时正好发生了定时器中断, 那么mepc就需要设定成在EXE阶段的B型指令的pc值而不是ID阶段的指令的pc值
        // 虽然这样返回的时候, 会再执行一次B型指令，严格来说不太正确
        // 但是没有关系, 因为正好再执行一次就可以跳转
        val flushreq_ex = Input(Bool())
        val EXpc = Input(UInt(64.W))

        val async_int_flag = Output(Bool())
        val int_jump_flag = Output(Bool())
        val int_jump_add = Output(UInt(64.W))
    })
    val mtime = RegInit(0.U(64.W))
    val mtimecmp = RegInit(0.U(64.W))
    mtime := Mux(io.mtime_enw, io.mtime_in, mtime + 1.U)
    mtimecmp := Mux(io.mtimecmp_enw, io.mtimecmp_in, mtimecmp)
    val time_int_flag = Wire(Bool())
    time_int_flag := (mtime > mtimecmp)
    // time_int_flag := false.B

    // val last_pc_notzero = RegInit(0.U(64.W))
    // last_pc_notzero := Mux((io.IDpc =/= 0.U), io.IDpc, last_pc_notzero)

    io.mtime_out    := mtime
    io.mtimecmp_out := mtimecmp
    

    val INT_IDLE = "b00".U
    val INT_SYNC = "b01".U
    val INT_ASYNC = "b10".U
    val INT_MRET = "b11".U

    // val int_state = Wire(UInt(2.W))
    // val int_state = Wire(UInt(2.W))
    val nxt_int_state = Wire(UInt(2.W))
    val int_state = RegNext(nxt_int_state, "b00".U)
    nxt_int_state := int_state
    io.async_int_flag := (int_state === INT_ASYNC)

    when(int_state === INT_SYNC)
    {
        io.csr_enw := true.B
        io.mstatus_out := Cat(io.mstatus_in(63, 8), io.mstatus_in(3), io.mstatus_in(6, 4), "b0".U, io.mstatus_in(2, 0))
        io.mepc_out := io.IDpc // 这个时候的pc不可能等于0
        io.mcause_out := "hb".U(64.W)
        io.mip_out := io.mip_in

        io.int_jump_flag := true.B
        io.int_jump_add := io.mtvec_in

        when(!io.global_int_en) // 中断使能关了说明进入了中断
        {
            nxt_int_state := INT_IDLE
        }
    }.elsewhen(int_state === INT_ASYNC)
    {
        io.csr_enw := true.B
        io.mstatus_out := Cat(io.mstatus_in(63, 8), io.mstatus_in(3), io.mstatus_in(6, 4), "b0".U, io.mstatus_in(2, 0))
        // io.mepc_out := Mux(io.flushreq_ex, io.ex_pc, io.IDpc)
        io.mepc_out := Mux(io.flushreq_ex, io.EXpc, io.IDpc)
        io.mcause_out := Mux(io.int_flag, "h800000000000000b".U(64.W), "h8000000000000007".U(64.W))
        io.mip_out := "h80".U(64.W) // MIP的MTIP位置1([7]) 

        io.int_jump_flag := true.B
        io.int_jump_add := io.mtvec_in

        when(!io.global_int_en)// 中断使能关了说明进入了中断
        // when(io.IFpc === io.mtvec_in)
        {
            nxt_int_state := INT_IDLE
        }
    }.elsewhen(int_state === INT_MRET)
    {
        io.csr_enw := true.B
        // io.mstatus_out := Cat(io.mstatus_in(63, 4), io.mstatus_in(7), io.mstatus_in(2, 0))
        io.mstatus_out := Cat(io.mstatus_in(63, 13), "b00".U(2.W), io.mstatus_in(10, 8), "b0".U(1.W), io.mstatus_in(6, 4), io.mstatus_in(7), io.mstatus_in(2, 0))
        io.mepc_out := io.mepc_in
        io.mcause_out := io.mcause_in
        io.mip_out := io.mip_in

        io.int_jump_flag := true.B
        io.int_jump_add := io.mepc_in

        when(io.IFpc === io.mepc_in)
        {
            nxt_int_state := INT_IDLE
        }
    }.elsewhen(int_state === INT_IDLE)
    {
        io.csr_enw      := false.B
        io.mstatus_out  := io.mstatus_in
        io.mepc_out     := io.mepc_in
        io.mcause_out   := io.mcause_in
        io.mip_out      := io.mip_in

        io.int_jump_flag    := false.B
        io.int_jump_add     := 0.U

        when(io.inst === "b0000000_00000_00000_000_00000_11100_11".U) // ecall
        {
            nxt_int_state := INT_SYNC
        }.elsewhen(io.global_int_en && (io.IDpc =/= 0.U) && (io.int_flag || (time_int_flag && io.mie_in(7))))
        {
            nxt_int_state := INT_ASYNC
        }.elsewhen(io.inst === "b0011000_00010_00000_000_00000_11100_11".U) // mret
        {
            nxt_int_state := INT_MRET
        }
    }.otherwise
    {
        io.csr_enw      := false.B
        io.mstatus_out  := io.mstatus_in
        io.mepc_out     := io.mepc_in
        io.mcause_out   := io.mcause_in
        io.mip_out      := io.mip_in

        io.int_jump_flag    := false.B
        io.int_jump_add     := 0.U
    }
}