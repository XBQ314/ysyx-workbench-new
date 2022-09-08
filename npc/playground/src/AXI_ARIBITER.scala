import chisel3._
import chisel3.util._
import xbqpackage._

class AXI_ARIBITER extends Module
{
    val io = IO(new Bundle
    {
        // ICache和memory接口
        val ICache_addr2mem    = Input(UInt(64.W))
        val ICache_data2mem    = Input(UInt(64.W)) // used when write
        val ICache_wmask2mem   = Input(UInt(8.W)) // used when write
        val ICache_valid2mem   = Input(Bool())
        val ICache_enw2mem     = Input(Bool()) // used when write

        val ICache_mem_data    = Output(UInt(64.W))
        val ICache_mem_ready   = Output(Bool())

        // DCache和memory接口
        val DCache_addr2mem    = Input(UInt(64.W))
        val DCache_data2mem    = Input(UInt(64.W)) // used when write
        val DCache_wmask2mem   = Input(UInt(8.W)) // used when write
        val DCache_valid2mem   = Input(Bool())
        val DCache_enw2mem     = Input(Bool()) // used when write

        val DCache_mem_data    = Output(UInt(64.W))
        val DCache_mem_ready   = Output(Bool())

        // 与AXIRW的接口
        val rw_valid_i  = Output(Bool())
        val enw_i       = Output(Bool())
        val rw_w_data_i = Output(UInt(64.W))
        val rw_addr_i   = Output(UInt(32.W))
        val rw_size_i   = Output(UInt(8.W))

        val rw_ready_o  = Input(Bool())
        val data_read_o = Input(UInt(64.W))
    })
    // 默认值
    io.ICache_mem_data      := 0.U
    io.ICache_mem_ready     := false.B
    io.DCache_mem_data      := 0.U
    io.DCache_mem_ready     := false.B
    io.rw_valid_i           := false.B 
    io.enw_i                := false.B 
    io.rw_w_data_i          := 0.U
    io.rw_addr_i            := 0.U
    io.rw_size_i            := 0.U

    // // 
    // io.rw_valid_i           := Mux(io.ICache_valid2mem, true.B, io.DCache_valid2mem)
    // io.enw_i                := Mux(io.ICache_valid2mem, io.ICache_enw2mem, io.DCache_enw2mem)
    // io.rw_w_data_i          := Mux(io.ICache_valid2mem, io.ICache_data2mem, io.DCache_data2mem)
    // io.rw_addr_i            := Mux(io.ICache_valid2mem, io.ICache_addr2mem, io.DCache_addr2mem)
    // io.rw_size_i            := Mux(io.ICache_valid2mem, io.ICache_wmask2mem, io.DCache_wmask2mem)

    // io.ICache_mem_data      := Mux(io.ICache_valid2mem, io.data_read_o, 0.U)
    // io.ICache_mem_ready     := Mux(io.ICache_valid2mem, io.rw_ready_o, false.B)

    // // io.DCache_mem_data      := Mux(io.ICache_valid2mem, 0.U, 
    // //                            Mux(io.DCache_valid2mem, io.data_read_o, 0.U))
    // io.DCache_mem_data      := Mux(io.ICache_valid2mem, 0.U, io.data_read_o)
    // io.DCache_mem_ready     := Mux(io.ICache_valid2mem, false.B,
    //                            Mux(io.DCache_valid2mem, io.rw_ready_o, false.B))

    val IDLE = "b000".U
    val ICACHE_OCCUPY = "b001".U
    val DCACHE_OCCUPY = "b010".U

    val nxt_state = Wire(UInt(3.W))
    val cur_state = RegNext(nxt_state, "b0000".U(4.W))
    nxt_state := cur_state
    when(cur_state === IDLE)
    {
        when(io.ICache_valid2mem)
        {
            nxt_state := ICACHE_OCCUPY
        }.elsewhen(io.DCache_valid2mem)
        {
            nxt_state := DCACHE_OCCUPY
        }
    }.elsewhen(cur_state === ICACHE_OCCUPY)
    {
        io.ICache_mem_data      := io.data_read_o
        io.ICache_mem_ready     := io.rw_ready_o
        io.rw_valid_i           := true.B
        io.enw_i                := io.ICache_enw2mem
        io.rw_w_data_i          := io.ICache_data2mem
        io.rw_addr_i            := io.ICache_addr2mem
        io.rw_size_i            := io.ICache_wmask2mem
        when(io.rw_ready_o)
        {
            nxt_state := IDLE
        }
    }.elsewhen(cur_state === DCACHE_OCCUPY)
    {
        io.DCache_mem_data      := io.data_read_o
        io.DCache_mem_ready     := io.rw_ready_o
        io.rw_valid_i           := true.B
        io.enw_i                := io.DCache_enw2mem
        io.rw_w_data_i          := io.DCache_data2mem
        io.rw_addr_i            := io.DCache_addr2mem
        io.rw_size_i            := io.DCache_wmask2mem
        when(io.rw_ready_o)
        {
            nxt_state := IDLE
        }
    }
}