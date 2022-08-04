import chisel3._
import chisel3.util._

class CLINT extends Module
{
    val io = IO(new Bundle
    {
        val inst = Input(UInt(32.W))
        val pc = Input(UInt(64.W))

        val global_int_en = Input(Bool())
        val int_flag = Input(Bool())

        val mstatus_in = Input(UInt(64.W))
        val mepc_in = Input(UInt(64.W))
        val mtvec_in = Input(UInt(64.W))

        val csr_enw = Output(Bool())
        val mstatus_out = Output(UInt(64.W))
        val mepc_out = Output(UInt(64.W))
        val mcause_out = Output(UInt(64.W))

        val int_jump_flag = Output(Bool())
        val int_jump_add = Output(UInt(64.W))
    })
    val INT_IDLE = "b00".U
    val INT_SYNC = "b01".U
    val INT_ASYNC = "b10".U
    val INT_MRET = "b11".U

    val int_state = Wire(UInt(2.W))
    when(io.inst === "b0000000_00000_00000_000_00000_11100_11".U) // ecall
    {
        int_state := INT_SYNC
    }.elsewhen(io.int_flag && io.global_int_en)
    {
        int_state := INT_ASYNC
    }.elsewhen(io.inst === "b0011000_00010_00000_000_00000_11100_11".U) // mret
    {
        int_state := INT_MRET
    }.otherwise
    {
        int_state := INT_IDLE
    }

    when(int_state === INT_SYNC)
    {
        io.csr_enw := true.B
        io.mstatus_out := Cat(io.mstatus_in(63, 8), io.mstatus_in(3), io.mstatus_in(6, 4), "b0".U, io.mstatus_in(2, 0))
        io.mepc_out := io.pc
        io.mcause_out := "hb".U(64.W)

        io.int_jump_flag := true.B
        io.int_jump_add := io.mtvec_in
    }.elsewhen(int_state === INT_ASYNC)
    {
        io.csr_enw := true.B
        io.mstatus_out := Cat(io.mstatus_in(63, 4), "b0".U, io.mstatus_in(2, 0))
        io.mepc_out := io.pc
        io.mcause_out := "h8000000000000007".U(64.W)

        io.int_jump_flag := true.B
        io.int_jump_add := io.mtvec_in
    }.elsewhen(int_state === INT_MRET)
    {
        io.csr_enw := true.B
        io.mstatus_out := Cat(io.mstatus_in(63, 4), io.mstatus_in(7), io.mstatus_in(2, 0))
        io.mepc_out := io.mepc_in
        io.mcause_out := "hb".U(64.W)

        io.int_jump_flag := true.B
        io.int_jump_add := io.mepc_in
    }.otherwise
    {
        io.csr_enw := false.B
        io.mstatus_out := 0.U
        io.mepc_out := 0.U
        io.mcause_out := 0.U

        io.int_jump_flag := false.B
        io.int_jump_add := 0.U
    }
}