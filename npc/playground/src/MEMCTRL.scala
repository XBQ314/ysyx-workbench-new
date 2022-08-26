import chisel3._
import chisel3.util._

class MEMCTRL extends Module
{
    val io = IO(new Bundle
    {
        val loadstore_flag = Input(Bool())
        val pc = Input(UInt(64.W))
        val addr = Input(UInt(64.W))
        val dcache_ready = Input(Bool())

        val dcache_valid = Output(Bool())
        val memstall_req = Output(Bool())
        val uncached_flag = Output(Bool())
    })
    val IDLE = "b000".U
    val LOADSTORE_VALID = "b001".U
    val BUSY = "b010".U
    val nxt_state = Wire(UInt(3.W))
    val cur_state = RegNext(nxt_state, "b000".U)
    val last_pc = RegNext(io.pc, 0.U(64.W)) // 记录上次的pc,来判断指令是否更新,防止一条ls指令触发两次读写

    io.dcache_valid := false.B
    io.memstall_req := false.B
    io.uncached_flag := (io.addr === "ha0000048".U(64.W) || io.addr === "ha00003f8".U(64.W))
    nxt_state := cur_state
    when(cur_state === IDLE)
    {
        when(io.loadstore_flag && last_pc =/= io.pc)
        {
            // loadstore_pc := io.pc
            io.memstall_req := true.B
            nxt_state := LOADSTORE_VALID
        }.otherwise
        {
            nxt_state := IDLE
        }
    }.elsewhen(cur_state === LOADSTORE_VALID)
    {
        io.dcache_valid := true.B
        io.memstall_req := true.B
        nxt_state := BUSY
    }.elsewhen(cur_state === BUSY)
    {
        io.memstall_req := true.B
        when(io.dcache_ready)
        {
            nxt_state := IDLE
        }.otherwise
        {
            nxt_state := BUSY
        }
    }
}