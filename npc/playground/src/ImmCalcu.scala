import chisel3._
import chisel3.util._

class ImmCalcu extends Module
{
    val io = IO(new Bundle
    {
        val inst = Input(UInt(32.W))
        val imm_sel = Input(UInt(3.W))

        val imm = Output(UInt(64.W))
    })

    io.imm := 0.U

    val immI = Cat(Fill(52, io.inst(31)), io.inst(31, 20))
    val immU = Cat(Fill(32, io.inst(31)), io.inst(31, 12), 0.U(12.W))
    val immS = Cat(Fill(52, io.inst(31)), io.inst(31, 25), io.inst(11, 7))
    val immB = Cat(Fill(52, io.inst(31)), io.inst(7), io.inst(30, 25), io.inst(11, 8), 0.U(1.W))
    val immJ = Cat(Fill(44, io.inst(31)), io.inst(19, 12), io.inst(20), io.inst(30, 21), 0.U(1.W))

    switch(io.imm_sel)
    {
    is("b000".U){io.imm := immI}
    is("b001".U){io.imm := immU}
    is("b010".U){io.imm := immS}
    is("b011".U){io.imm := immB}
    is("b100".U){io.imm := immJ}
    }
}