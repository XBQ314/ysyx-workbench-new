import chisel3._
import chisel3.util._

// class IFUctrl extends Bundle
// {
//     val ifuMux1 = UInt(2.W) // from IDU
//     val ifuMux2 = UInt(1.W) // from IDU
//     val ifuOutMux = UInt(1.W) // from IDU
// }
class IFU extends Module
{
    val io = IO(new Bundle
    {
        val jump_flag = Input(Bool())
        val pc_next = Input(UInt(64.W))
        val enIFU = Input(Bool())
        val data = Input(UInt(64.W))
        val ready = Input(Bool())

        val pc2cache_valid = Output(Bool())
        val ifu_stall_req = Output(Bool())
        val pc = Output(UInt(64.W))
        val inst = Output(UInt(32.W))
    })
    val regPC = RegInit(("h80000000".U(64.W)))

    val READ = "b0".U
    val WAIT = "b1".U
    val nxt_state = Wire(UInt(1.W))
    val cur_state = RegNext(nxt_state, "b0".U)

    io.inst := Mux(io.pc(2), io.data(63, 32), io.data(31, 0))
    io.pc := regPC
    
    when(io.jump_flag && io.enIFU && io.ready && io.pc2cache_valid)
    {
        regPC := io.pc_next
    }.elsewhen(!io.jump_flag && io.enIFU && io.ready && io.pc2cache_valid)
    {
        regPC := regPC + 4.U
    }.otherwise
    {
        regPC := regPC
    }

    when(cur_state === READ)
    {
        io.pc2cache_valid := true.B
        io.ifu_stall_req := true.B
        when(io.pc2cache_valid && io.ready && !io.enIFU)
        {
            nxt_state := WAIT
            // io.ifu_stall_req := false.B
        }.elsewhen(io.pc2cache_valid && io.ready && io.enIFU)
        {
            nxt_state := READ
            io.ifu_stall_req := false.B
        }.otherwise
        {
            nxt_state := READ
        }
    }.otherwise
    {
        io.pc2cache_valid := false.B
        // io.ifu_stall_req := false.B
        io.ifu_stall_req := true.B
        when(io.enIFU)
        {
            nxt_state := READ
        }.otherwise
        {
            nxt_state := WAIT
        }
    }
    // io.pc2cache_valid := true.B
    // io.ifu_stall_req := true.B
    // io.pc := regPC
    // when(io.ready)
    // {
    //     io.pc2cache_valid := false.B
    //     io.ifu_stall_req := false.B
    //     when(io.enIFU)
    //     {
    //         pc_update := true.B
    //         nxt_state := FETCH
    //     }
    //     // nxt_state := DONE
    // }

    // when(cur_state === FETCH)
    // {
    //     io.pc2cache_valid := true.B
    //     io.ifu_stall_req := true.B
    //     io.pc := regPC
    //     when(io.ready)
    //     {
    //         nxt_state := DONE
    //     }.otherwise
    //     {
    //         nxt_state := FETCH
    //     }
    // }.elsewhen(cur_state === DONE)
    // {
    //     io.pc2cache_valid := false.B
    //     io.ifu_stall_req := false.B
    //     when(io.enIFU)
    //     {
    //         pc_update := true.B
    //         nxt_state := FETCH
    //     }
    // }
}