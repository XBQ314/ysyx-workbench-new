import chisel3._
import chisel3.util._

class IFUctrl extends Bundle
{
    val ifuMux1 = UInt(2.W) // from IDU
    val ifuMux2 = UInt(1.W) // from IDU
    val ifuOutMux = UInt(1.W) // from IDU
}
class IFU extends Module
{
    val io = IO(new Bundle
    {
        val jump_flag = Input(Bool())
        val pc_next = Input(UInt(64.W))
        val enIFU = Input(Bool())

        val pc = Output(UInt(64.W))
    })
    val regPC = RegInit(("h80000000".U(64.W)))
    // val regPC = RegEnable(0.U, "h80000000".U(64.W), io.enIFU)
    
    when(io.jump_flag && io.enIFU)
    {
        regPC := io.pc_next
    }.elsewhen(!io.jump_flag && io.enIFU)
    {
        regPC := regPC + 4.U
    }.otherwise
    {
        regPC := regPC
    }
    io.pc := regPC
}
// class IFU extends Module
// {
//     val io = IO(new Bundle
//     {
//         val regfile_out1 = Input(UInt(64.W)) //from regFile

//         val IFUctrl = Input(new IFUctrl)
//         val ALUout_data = Input(UInt(1.W))
//         val imm = Input(UInt(64.W)) //from IDU

//         val pc = Output(UInt(64.W)) //to alu and top
//     })
//     val regPC = RegInit(("h80000000".U(64.W)))

//     val addSrc1 = Wire(UInt(64.W))
//     val addSrc2 = Wire(UInt(64.W))
//     val addOut = Wire(UInt(64.W))

//     io.pc:=regPC

//     addSrc1 := 0.U
//     switch(io.IFUctrl.ifuMux1)
//     {
//     is(0.U){addSrc1:=4.U}
//     is(1.U){addSrc1:=io.imm}
//     is(2.U){addSrc1:=Mux(io.ALUout_data === 1.U, io.imm, 4.U)}
//     }

//     addSrc2 := 0.U
//     switch(io.IFUctrl.ifuMux2)
//     {
//     is(0.U){addSrc2:=regPC}
//     is(1.U){addSrc2:=io.regfile_out1}
//     }

//     addOut := addSrc1+addSrc2
//     switch(io.IFUctrl.ifuOutMux)
//     {
//     is(0.U){regPC:=addOut}
//     is(1.U){regPC:=addOut&("hFF_FF_FF_FF_FF_FF_FF_FE".U)}
//     }
// }