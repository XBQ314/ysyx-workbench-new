import chisel3._
import chisel3.util._

class MEM2WB extends Module
{
    val io = IO(new Bundle
    {
        val enMEM2WB = Input(Bool())
        val flush = Input(Bool())

        val MEMrd      = Input(UInt(5.W))
        val MEMenw     = Input(UInt(1.W))
        val MEMcsr_enw = Input(UInt(1.W))
        val MEMwrb2reg = Input(UInt(64.W))
        val MEMcsr_rd  = Input(UInt(8.W))
        val MEMwrb2csr = Input(UInt(64.W))
        val MEMmemout  = Input(UInt(64.W))
        val MEMLOADctrl= Input(UInt(3.W))
        val MEMecall_flag=Input(Bool())
        val MEMLoad_flag=Input(Bool())
        val MEMpc      = Input(UInt(64.W))
        val MEMinst    = Input(UInt(32.W))


        val WBrd       = Output(UInt(5.W))
        val WBenw      = Output(UInt(1.W))
        val WBcsr_enw  = Output(UInt(1.W))
        val WBwrb2reg  = Output(UInt(64.W))
        val WBcsr_rd   = Output(UInt(8.W))
        val WBwrb2csr  = Output(UInt(64.W))
        val WBmemout   = Output(UInt(64.W))
        val WBLOADctrl = Output(UInt(3.W))
        val WBecall_flag=Output(Bool())
        val WBLoad_flag= Output(Bool())
        val WBpc       = Output(UInt(64.W))
        val WBinst     = Output(UInt(32.W))
    })

    val WBrd_reg       = RegEnable(io.MEMrd      , 0.U, io.enMEM2WB)
    val WBenw_reg      = RegEnable(io.MEMenw     , 0.U, io.enMEM2WB)
    val WBcsr_enw_reg  = RegEnable(io.MEMcsr_enw , 0.U, io.enMEM2WB)
    val WBwrb2reg_reg  = RegEnable(io.MEMwrb2reg , 0.U, io.enMEM2WB)
    val WBcsr_rd_reg   = RegEnable(io.MEMcsr_rd  , 0.U, io.enMEM2WB)
    val WBwrb2csr_reg  = RegEnable(io.MEMwrb2csr , 0.U, io.enMEM2WB)
    val WBmemout_reg   = RegEnable(io.MEMmemout  , 0.U, io.enMEM2WB)
    val WBLOADctrl_reg = RegEnable(io.MEMLOADctrl, 0.U, io.enMEM2WB)
    val WBecall_flag_reg=RegEnable(io.MEMecall_flag,false.B, io.enMEM2WB)
    val WBLoad_flag_reg= RegEnable(io.MEMLoad_flag, false.B, io.enMEM2WB)
    val WBpc_reg       = RegEnable(io.MEMpc      , 0.U, io.enMEM2WB)
    val WBinst_reg     = RegEnable(io.MEMinst    , 0.U, io.enMEM2WB)

    when(io.flush)
    {
        WBrd_reg       := 0.U
        WBenw_reg      := 0.U
        WBcsr_enw_reg  := 0.U
        WBwrb2reg_reg  := 0.U
        WBcsr_rd_reg   := 0.U
        WBwrb2csr_reg  := 0.U
        WBmemout_reg   := 0.U
        WBLOADctrl_reg := 0.U
        WBecall_flag_reg:=false.B
        WBLoad_flag_reg:= false.B
        WBpc_reg       := "h00000000".U(64.W)
        WBinst_reg     := "h00000013".U(64.W)
    }

    io.WBrd         := WBrd_reg    
    io.WBenw        := WBenw_reg 
    io.WBcsr_enw    := WBcsr_enw_reg  
    io.WBwrb2reg    := WBwrb2reg_reg
    io.WBcsr_rd     := WBcsr_rd_reg
    io.WBwrb2csr    := WBwrb2csr_reg
    io.WBmemout     := WBmemout_reg
    io.WBLOADctrl   := WBLOADctrl_reg
    io.WBecall_flag := WBecall_flag_reg
    io.WBLoad_flag  := WBLoad_flag_reg
    io.WBpc         := WBpc_reg    
    io.WBinst       := WBinst_reg  
}