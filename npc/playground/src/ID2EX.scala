import chisel3._
import chisel3.util._

class ID2EX extends Module
{
    val io = IO(new Bundle
    {
        val enID2EX = Input(Bool())
        val flush = Input(Bool())

        val IDimm = Input(UInt(64.W))
        val IDshamt = Input(UInt(6.W))
        val IDALUctrl = Input(new ALUctrl())
        val IDIFUctrl = Input(new IFUctrl())
        val IDLOADctrl = Input(UInt(3.W))
        val IDWmask = Input(UInt(8.W))
        val IDenw = Input(UInt(1.W))
        val IDcsr_enw = Input(UInt(1.W))
        val IDrd = Input(UInt(5.W))
        val IDregout1 = Input(UInt(64.W))
        val IDregout2 = Input(UInt(64.W))
        val IDcsr_rd = Input(UInt(8.W))
        val IDcsrout = Input(UInt(64.W))
        val IDecall_flag = Input(Bool())
        val IDBtype_flag = Input(Bool())
        val IDLoad_flag = Input(Bool())
        val IDpc = Input(UInt(64.W))
        val IDinst = Input(UInt(32.W))



        val EXimm = Output(UInt(64.W))
        val EXshamt = Output(UInt(6.W))
        val EXALUctrl = Output(new ALUctrl())
        val EXIFUctrl = Output(new IFUctrl())
        val EXLOADctrl = Output(UInt(3.W))
        val EXWmask = Output(UInt(8.W))
        val EXenw = Output(UInt(1.W))
        val EXcsr_enw = Output(UInt(1.W))
        val EXrd = Output(UInt(5.W))
        val EXregout1 = Output(UInt(64.W))
        val EXregout2 = Output(UInt(64.W))
        val EXcsr_rd = Output(UInt(8.W))
        val EXcsrout = Output(UInt(64.W))
        val EXecall_flag = Output(Bool())
        val EXBtype_flag = Output(Bool())
        val EXLoad_flag = Output(Bool())
        val EXpc = Output(UInt(64.W))
        val EXinst = Output(UInt(32.W))
    })

    val EXimm_reg       = RegEnable(io.IDimm        , 0.U, io.enID2EX)
    val EXshamt_reg     = RegEnable(io.IDshamt      , 0.U, io.enID2EX)
    val EXALUctrl_reg   = RegEnable(io.IDALUctrl    , 0.U.asTypeOf(chiselTypeOf(io.IDALUctrl)), io.enID2EX)
    val EXIFUctrl_reg   = RegEnable(io.IDIFUctrl    , 0.U.asTypeOf(chiselTypeOf(io.IDIFUctrl)), io.enID2EX)
    val EXLOADctrl_reg  = RegEnable(io.IDLOADctrl   , 0.U, io.enID2EX)
    val EXWmask_reg     = RegEnable(io.IDWmask      , 0.U, io.enID2EX)
    val EXenw_reg       = RegEnable(io.IDenw        , 0.U, io.enID2EX)
    val EXcsr_enw_reg   = RegEnable(io.IDcsr_enw    , 0.U, io.enID2EX)
    val EXrd_reg        = RegEnable(io.IDrd         , 0.U, io.enID2EX)
    val EXregout1_reg   = RegEnable(io.IDregout1    , 0.U, io.enID2EX)
    val EXregout2_reg   = RegEnable(io.IDregout2    , 0.U, io.enID2EX)
    val EXcsr_rd_reg    = RegEnable(io.IDcsr_rd     , 0.U, io.enID2EX)
    val EXcsrout_reg    = RegEnable(io.IDcsrout     , 0.U, io.enID2EX)
    val EXecall_flag_reg= RegEnable(io.IDecall_flag , false.B, io.enID2EX)
    val EXBtype_flag_reg= RegEnable(io.IDBtype_flag , false.B, io.enID2EX)
    val EXLoad_flag_reg = RegEnable(io.IDLoad_flag  , false.B, io.enID2EX)
    val EXpc_reg        = RegEnable(io.IDpc         , 0.U, io.enID2EX)
    val EXinst_reg      = RegEnable(io.IDinst       , 0.U, io.enID2EX)

    when(io.flush)
    {
        EXimm_reg       := 0.U
        EXshamt_reg     := 0.U
        EXALUctrl_reg   := 0.U.asTypeOf(chiselTypeOf(io.IDALUctrl))
        EXIFUctrl_reg   := 0.U.asTypeOf(chiselTypeOf(io.IDIFUctrl))
        EXLOADctrl_reg  := 0.U
        EXWmask_reg     := 0.U
        EXenw_reg       := 0.U
        EXcsr_enw_reg   := 0.U
        EXrd_reg        := 0.U
        EXregout1_reg   := 0.U
        EXregout2_reg   := 0.U
        EXcsr_rd_reg    := 0.U
        EXcsrout_reg    := 0.U
        EXecall_flag_reg:= false.B
        EXBtype_flag_reg:= false.B
        EXLoad_flag_reg := false.B
        EXpc_reg        := "h00000000".U(64.W)
        EXinst_reg      := "h00000013".U(64.W)
    }

    io.EXimm            := EXimm_reg     
    io.EXshamt          := EXshamt_reg   
    io.EXALUctrl        := EXALUctrl_reg 
    io.EXIFUctrl        := EXIFUctrl_reg 
    io.EXLOADctrl       := EXLOADctrl_reg
    io.EXWmask          := EXWmask_reg   
    io.EXenw            := EXenw_reg
    io.EXcsr_enw        := EXcsr_enw_reg
    io.EXrd             := EXrd_reg      
    io.EXregout1        := EXregout1_reg 
    io.EXregout2        := EXregout2_reg
    io.EXcsr_rd         := EXcsr_rd_reg
    io.EXcsrout         := EXcsrout_reg
    io.EXecall_flag     := EXecall_flag_reg
    io.EXBtype_flag     := EXBtype_flag_reg 
    io.EXLoad_flag      := EXLoad_flag_reg 
    io.EXpc             := EXpc_reg
    io.EXinst           := EXinst_reg
}