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
        val MEMclint_enw=Input(Bool())
        val MEMclint_mstatus = Input(UInt(64.W))
        val MEMclint_mepc =    Input(UInt(64.W))
        val MEMclint_mcause =  Input(UInt(64.W))
        val MEMclint_mip =  Input(UInt(64.W))
        // val MEMLoad_flag=Input(Bool())
        val MEMwaddr   = Input(UInt(64.W))
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
        val WBclint_enw= Output(Bool())
        val WBclint_mstatus = Output(UInt(64.W))
        val WBclint_mepc    = Output(UInt(64.W))
        val WBclint_mcause  = Output(UInt(64.W))
        val WBclint_mip     = Output(UInt(64.W))
        // val WBLoad_flag= Output(Bool())
        val WBwaddr   = Output(UInt(64.W))
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
    val WBclint_enw_reg= RegEnable(io.MEMclint_enw, false.B, io.enMEM2WB)
    val WBclint_mstatus_reg= RegEnable(io.MEMclint_mstatus, 0.U, io.enMEM2WB)
    val WBclint_mepc_reg   = RegEnable(io.MEMclint_mepc, 0.U, io.enMEM2WB)
    val WBclint_mcause_reg = RegEnable(io.MEMclint_mcause, 0.U, io.enMEM2WB)
    val WBclint_mip_reg= RegEnable(io.MEMclint_mip, false.B, io.enMEM2WB)
    // val WBLoad_flag_reg= RegEnable(io.MEMLoad_flag, false.B, io.enMEM2WB)
    val WBwaddr_reg    = RegEnable(io.MEMwaddr,     0.U, io.enMEM2WB)
    val WBpc_reg       = RegEnable(io.MEMpc,        0.U, io.enMEM2WB)
    val WBinst_reg     = RegEnable(io.MEMinst,      0.U, io.enMEM2WB)

    when(io.flush && io.enMEM2WB)
    {
        WBrd_reg        := 0.U
        WBenw_reg       := 0.U
        WBcsr_enw_reg   := 0.U
        WBwrb2reg_reg   := 0.U
        WBcsr_rd_reg    := 0.U
        WBwrb2csr_reg   := 0.U
        WBmemout_reg    := 0.U
        WBLOADctrl_reg  := 0.U
        WBclint_enw_reg := false.B
        WBclint_mstatus_reg := 0.U
        WBclint_mepc_reg    := 0.U
        WBclint_mcause_reg  := 0.U
        WBclint_mip_reg     := 0.U
        // WBLoad_flag_reg := false.B
        WBwaddr_reg     := 0.U
        WBpc_reg        := "h00000000".U(64.W)
        WBinst_reg      := "h00000013".U(64.W)
    }

    io.WBrd         := WBrd_reg    
    io.WBenw        := WBenw_reg 
    io.WBcsr_enw    := WBcsr_enw_reg  
    io.WBwrb2reg    := WBwrb2reg_reg
    io.WBcsr_rd     := WBcsr_rd_reg
    io.WBwrb2csr    := WBwrb2csr_reg
    io.WBmemout     := WBmemout_reg
    io.WBLOADctrl   := WBLOADctrl_reg
    io.WBclint_enw  := WBclint_enw_reg
    io.WBclint_mstatus  := WBclint_mstatus_reg
    io.WBclint_mepc     := WBclint_mepc_reg   
    io.WBclint_mcause   := WBclint_mcause_reg
    io.WBclint_mip      := WBclint_mip_reg
    // io.WBLoad_flag  := WBLoad_flag_reg
    io.WBwaddr      := WBwaddr_reg
    io.WBpc         := WBpc_reg    
    io.WBinst       := WBinst_reg  
}