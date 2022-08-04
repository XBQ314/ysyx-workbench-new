import chisel3._
import chisel3.util._

class CTRL extends Module
{
    val io = IO(new Bundle
    {
        val flushreq_id = Input(Bool())
        val flushreq_ex = Input(Bool())

        val id_rs1 = Input(UInt(5.W))
        val id_rs2 = Input(UInt(5.W))
        val ex_rd  = Input(UInt(5.W))
        val ex_enw = Input(UInt(1.W))
        val mem_rd = Input(UInt(5.W))
        val mem_enw= Input(UInt(1.W))
        val wb_rd  = Input(UInt(5.W))
        val wb_enw = Input(UInt(1.W))

        val loadflag_ex = Input(Bool())
        val mulstall_req = Input(Bool())

        val feedflag_ex2id_rs1 = Output(Bool())
        val feedflag_ex2id_rs2 = Output(Bool())
        val feedflag_mem2id_rs1 = Output(Bool())
        val feedflag_mem2id_rs2 = Output(Bool())
        val feedflag_wb2id_rs1 = Output(Bool())
        val feedflag_wb2id_rs2 = Output(Bool())

        val stall_ifu    = Output(Bool())
        val stall_if2id  = Output(Bool())
        val stall_id2ex  = Output(Bool())
        val stall_ex2mem = Output(Bool())
        val stall_mem2wb = Output(Bool())

        val flush_if2id  = Output(Bool())
        val flush_id2ex  = Output(Bool())
        val flush_ex2mem = Output(Bool())
        val flush_mem2wb = Output(Bool())
    })
    io.stall_ifu    := false.B
    io.stall_if2id  := false.B
    io.stall_id2ex  := false.B
    io.stall_ex2mem := false.B
    io.stall_mem2wb := false.B

    io.flush_if2id  := false.B
    io.flush_id2ex  := false.B
    io.flush_ex2mem := false.B
    io.flush_mem2wb := false.B

    // when(io.flushreq_ecall)
    // {
    //     io.flush_if2id  := true.B
    //     io.flush_id2ex  := true.B
    //     io.flush_ex2mem := true.B
    //     io.flush_mem2wb := true.B  
    // }.else
    when(io.flushreq_ex) // B型指令
    {
        io.flush_if2id  := true.B
        io.flush_id2ex  := true.B
        io.flush_ex2mem := false.B
        io.flush_mem2wb := false.B
    }.elsewhen(io.flushreq_id) // jal与jalr
    {
        io.flush_if2id  := true.B
        io.flush_id2ex  := false.B
        io.flush_ex2mem := false.B
        io.flush_mem2wb := false.B   
    }


    io.feedflag_ex2id_rs1   := Mux(io.ex_enw === 1.U && (io.id_rs1 === io.ex_rd), true.B, false.B)
    io.feedflag_ex2id_rs2   := Mux(io.ex_enw === 1.U && (io.id_rs2 === io.ex_rd), true.B, false.B)
    io.feedflag_mem2id_rs1  := Mux(io.mem_enw === 1.U && (io.id_rs1 === io.mem_rd), true.B, false.B)   
    io.feedflag_mem2id_rs2  := Mux(io.mem_enw === 1.U && (io.id_rs2 === io.mem_rd), true.B, false.B)
    io.feedflag_wb2id_rs1   := Mux(io.wb_enw === 1.U && (io.id_rs1 === io.wb_rd), true.B, false.B)
    io.feedflag_wb2id_rs2   := Mux(io.wb_enw === 1.U && (io.id_rs2 === io.wb_rd), true.B, false.B)

    when(io.loadflag_ex && (io.feedflag_ex2id_rs1 || io.feedflag_ex2id_rs2))
    {
        io.flush_if2id  := false.B
        io.flush_id2ex  := true.B
        io.flush_ex2mem := false.B
        io.flush_mem2wb := false.B

        io.stall_ifu    := true.B
        io.stall_if2id  := true.B
        io.stall_id2ex  := false.B
        io.stall_ex2mem := false.B
        io.stall_mem2wb := false.B
    }
    when(io.mulstall_req)
    {
        io.stall_ifu    := true.B
        io.stall_if2id  := true.B
        io.stall_id2ex  := true.B
        io.stall_ex2mem := true.B
        io.stall_mem2wb := true.B
    }
}