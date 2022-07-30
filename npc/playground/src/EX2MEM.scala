import chisel3._
import chisel3.util._

class EX2MEM extends Module
{
    val io = IO(new Bundle
    {
        val enEX2MEM = Input(Bool())
        val flush = Input(Bool())

        val EXraddr     = Input(UInt(64.W))
        val EXwaddr     = Input(UInt(64.W))
        val EXwdata     = Input(UInt(64.W))
        val EXwmask     = Input(UInt(8.W))
        val EXLOADctrl  = Input(UInt(3.W))
        val EXrd        = Input(UInt(5.W))
        val EXenw       = Input(UInt(1.W))
        val EXcsr_enw   = Input(UInt(1.W))
        val EXwrb2reg   = Input(UInt(64.W)) // data that write back to Regfiles
        val EXcsr_rd    = Input(UInt(8.W))
        val EXwrb2csr   = Input(UInt(64.W)) // data that write back to csr
        val EXecall_flag= Input(Bool())
        val EXLoad_flag = Input(Bool())
        val EXpc        = Input(UInt(64.W))
        val EXinst      = Input(UInt(32.W))



        val MEMraddr    = Output(UInt(64.W))
        val MEMwaddr    = Output(UInt(64.W))
        val MEMwdata    = Output(UInt(64.W))
        val MEMwmask    = Output(UInt(8.W))
        val MEMLOADctrl = Output(UInt(3.W))
        val MEMrd       = Output(UInt(5.W))
        val MEMenw      = Output(UInt(1.W))
        val MEMcsr_enw  = Output(UInt(1.W))
        val MEMwrb2reg  = Output(UInt(64.W))
        val MEMcsr_rd   = Output(UInt(8.W))
        val MEMwrb2csr  = Output(UInt(64.W))
        val MEMecall_flag=Output(Bool())
        val MEMLoad_flag= Output(Bool())
        val MEMpc       = Output(UInt(64.W))
        val MEMinst     = Output(UInt(32.W))
    })

    val MEMraddr_reg    = RegEnable(io.EXraddr   , 0.U, io.enEX2MEM)
    val MEMwaddr_reg    = RegEnable(io.EXwaddr   , 0.U, io.enEX2MEM)
    val MEMwdata_reg    = RegEnable(io.EXwdata   , 0.U, io.enEX2MEM)
    val MEMwmask_reg    = RegEnable(io.EXwmask   , 0.U, io.enEX2MEM)
    val MEMLOADctrl_reg = RegEnable(io.EXLOADctrl, 0.U, io.enEX2MEM)
    val MEMrd_reg       = RegEnable(io.EXrd      , 0.U, io.enEX2MEM)
    val MEMenw_reg      = RegEnable(io.EXenw     , 0.U, io.enEX2MEM)
    val MEMcsr_enw_reg  = RegEnable(io.EXcsr_enw , 0.U, io.enEX2MEM)
    val MEMwrb2reg_reg  = RegEnable(io.EXwrb2reg , 0.U, io.enEX2MEM)
    val MEMcsr_rd_reg   = RegEnable(io.EXcsr_rd  , 0.U, io.enEX2MEM)
    val MEMwrb2csr_reg  = RegEnable(io.EXwrb2csr , 0.U, io.enEX2MEM)
    val MEMecall_flag_reg=RegEnable(io.EXecall_flag, false.B, io.enEX2MEM)
    val MEMLoad_flag_reg= RegEnable(io.EXLoad_flag , false.B, io.enEX2MEM)
    val MEMpc_reg       = RegEnable(io.EXpc      , 0.U, io.enEX2MEM)
    val MEMinst_reg     = RegEnable(io.EXinst    , 0.U, io.enEX2MEM)

    when(io.flush)
    {
        MEMraddr_reg    := 0.U
        MEMwaddr_reg    := 0.U
        MEMwdata_reg    := 0.U
        MEMwmask_reg    := 0.U
        MEMLOADctrl_reg := 0.U
        MEMrd_reg       := 0.U
        MEMenw_reg      := 0.U
        MEMcsr_enw_reg  := 0.U
        MEMwrb2reg_reg  := 0.U
        MEMcsr_rd_reg   := 0.U
        MEMwrb2csr_reg  := 0.U
        MEMecall_flag_reg:=false.B
        MEMLoad_flag_reg:= false.B
        MEMpc_reg       := "h00000000".U(64.W)
        MEMinst_reg     := "h00000013".U(64.W)
    }

    io.MEMraddr    := MEMraddr_reg   
    io.MEMwaddr    := MEMwaddr_reg   
    io.MEMwdata    := MEMwdata_reg   
    io.MEMwmask    := MEMwmask_reg   
    io.MEMLOADctrl := MEMLOADctrl_reg
    io.MEMrd       := MEMrd_reg      
    io.MEMenw      := MEMenw_reg
    io.MEMcsr_enw  := MEMcsr_enw_reg   
    io.MEMwrb2reg  := MEMwrb2reg_reg
    io.MEMcsr_rd   := MEMcsr_rd_reg
    io.MEMwrb2csr  := MEMwrb2csr_reg
    io.MEMecall_flag:=MEMecall_flag_reg
    io.MEMLoad_flag:= MEMLoad_flag_reg
    io.MEMpc       := MEMpc_reg      
    io.MEMinst     := MEMinst_reg    
}