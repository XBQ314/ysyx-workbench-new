import chisel3._
import chisel3.util._

class IF2ID extends Module
{
    val io = IO(new Bundle
    {
        val enIF2ID = Input(Bool())
        val flush = Input(Bool())

        val IFpc = Input(UInt(64.W))
        val IFinst = Input(UInt(32.W))

        val IDpc = Output(UInt(64.W))
        val IDinst = Output(UInt(32.W))
    })

    val IDpc_reg   = RegEnable(io.IFpc, 0.U, io.enIF2ID)
    val IDinst_reg = RegEnable(io.IFinst, 0.U(32.W), io.enIF2ID)

    when(io.flush && io.enIF2ID)
    {
        IDpc_reg   := "h00000000".U(64.W)
        IDinst_reg := "h00000013".U(64.W)
    }

    io.IDpc := IDpc_reg
    io.IDinst := IDinst_reg
}