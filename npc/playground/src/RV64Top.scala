import chisel3._
import chisel3.util._

// class RV64Top extends Module
// {
//     val io = IO(new Bundle
//     {
//         val pc = Output(UInt(64.W))
//         val inst = Output(UInt(32.W))

//         // val alu_out = Output(UInt(64.W))
//     })

//     val IFU1 = Module(new IFU())
//     val IFU_DPI1 = Module(new IFU_DPI())
//     val IDU1 = Module(new IDU())
//     val ALU1 = Module(new ALU())
//     val RegisterFiles1 = Module(new RegisterFiles())
//     val MEM1 = Module(new MEM())

//     //IFU input
//     IFU1.io.regfile_out1 := RegisterFiles1.io.regfile_out1
//     IFU1.io.IFUctrl := IDU1.io.IFUctrl
//     IFU1.io.ALUout_data := ALU1.io.ALUout_data(0)
//     IFU1.io.imm := IDU1.io.imm
//     IFU1.io.regfile_out1 := RegisterFiles1.io.regfile_out1
//     //IFU_DPI input
//     IFU_DPI1.io.pc := IFU1.io.pc
    
//     //IDU input
//     IDU1.io.inst := IFU_DPI1.io.inst

//     //ALU input
//     ALU1.io.regfile_out1 := RegisterFiles1.io.regfile_out1
//     ALU1.io.regfile_out2 := RegisterFiles1.io.regfile_out2
//     ALU1.io.imm := IDU1.io.imm
//     ALU1.io.shamt := IDU1.io.shamt
//     ALU1.io.pc := IFU1.io.pc
//     ALU1.io.ALUctrl := IDU1.io.ALUctrl
//     ALU1.io.pc := IFU1.io.pc

//     //RegFile input
//     RegisterFiles1.io.clock := clock
//     RegisterFiles1.io.reset := reset
//     RegisterFiles1.io.read1_idx := IDU1.io.rs1
//     RegisterFiles1.io.read2_idx := IDU1.io.rs2
//     RegisterFiles1.io.write_idx := IDU1.io.rd
//     RegisterFiles1.io.enw := IDU1.io.enw
//     RegisterFiles1.io.in_data := Mux(IDU1.io.LOADctrl === "b000".U, ALU1.io.ALUout_data, MEM1.io.rdata)

//     //MEM input
//     MEM1.io.raddr := ALU1.io.ALUout_data
//     MEM1.io.waddr := ALU1.io.ALUout_data
//     MEM1.io.wdata := RegisterFiles1.io.regfile_out2
//     MEM1.io.wmask := IDU1.io.Wmask
//     MEM1.io.LOADctrl := IDU1.io.LOADctrl

//     //top input
//     io.pc := IFU1.io.pc
//     io.inst := IFU_DPI1.io.inst
// }

// pipline
class RV64Top extends Module
{
    val io = IO(new Bundle
    {
        val pc = Output(UInt(64.W))
        val inst = Output(UInt(32.W))

        // val alu_out = Output(UInt(64.W))
    })
    val IFU0 = Module(new IFU())
    val IFU_DPI0 = Module(new IFU_DPI())
    val IF2ID0 = Module(new IF2ID())
    val IDU0 = Module(new IDU())
    val ID2EX0 = Module(new ID2EX())
    val ALU0 = Module(new ALU())
    val RegisterFiles0 = Module(new RegisterFiles())
    val EX2MEM0 = Module(new EX2MEM())
    val MEM0 = Module(new MEM())
    val MEM2WB0 = Module(new MEM2WB())
    val CTRL0 = Module(new CTRL())
    val CSR0 = Module(new CSR())

    //IF and ID
    // IFU0.io.regfile_out1 := RegisterFiles0.io.regfile_out1
    // IFU0.io.IFUctrl      := IDU0.io.IFUctrl
    // IFU0.io.ALUout_data  := ALU0.io.ALUout_data(0)
    // IFU0.io.imm          := IDU0.io.imm
    IFU0.io.jump_flag := IDU0.io.jump_flag || ALU0.io.jump_flag
    // IFU0.io.pc_next   := Mux(MEM2WB0.io.WBecall_flag, CSR0.io.mtvec_out,
    //                      Mux(ALU0.io.jump_flag, ALU0.io.pc_next, 
    //                      Mux(IDU0.io.jump_flag, IDU0.io.pc_next, 0.U))) // check the priority, later is higher
    IFU0.io.pc_next   := Mux(ALU0.io.jump_flag, ALU0.io.pc_next, 
                         Mux(IDU0.io.jump_flag, IDU0.io.pc_next, 0.U))
                         // 存在数据冒险的风险，没有解决
    IFU0.io.enIFU     := !CTRL0.io.stall_ifu
    IFU_DPI0.io.pc    := IFU0.io.pc

    IF2ID0.io.enIF2ID := !CTRL0.io.stall_if2id
    IF2ID0.io.flush   := CTRL0.io.flush_if2id

    IF2ID0.io.IFpc    := IFU0.io.pc
    IF2ID0.io.IFinst  := IFU_DPI0.io.inst

    IDU0.io.pc   := IF2ID0.io.IDpc
    IDU0.io.inst := IF2ID0.io.IDinst
    IDU0.io.regfile_out1 := ID2EX0.io.IDregout1
    IDU0.io.mtvec_out := CSR0.io.mtvec_out
    IDU0.io.mepc_out := CSR0.io.mepc_out

    //ID and EX
    ID2EX0.io.enID2EX   := !CTRL0.io.stall_id2ex
    ID2EX0.io.flush     := CTRL0.io.flush_id2ex

    ID2EX0.io.IDimm         := IDU0.io.imm
    ID2EX0.io.IDshamt       := IDU0.io.shamt
    ID2EX0.io.IDALUctrl     := IDU0.io.ALUctrl
    ID2EX0.io.IDIFUctrl     := IDU0.io.IFUctrl
    ID2EX0.io.IDLOADctrl    := IDU0.io.LOADctrl
    ID2EX0.io.IDWmask       := IDU0.io.Wmask
    ID2EX0.io.IDenw         := IDU0.io.enw
    ID2EX0.io.IDcsr_enw     := IDU0.io.csr_enw
    ID2EX0.io.IDrd          := IDU0.io.rd
    // ID2EX0.io.IDregout1     := RegisterFiles0.io.regfile_out1
    // ID2EX0.io.IDregout2     := RegisterFiles0.io.regfile_out2
    // ID2EX0.io.IDregout1     := Mux(CTRL0.io.feedflag_ex2id_rs1, ALU0.io.ALUout_data,
    //                            Mux(CTRL0.io.feedflag_mem2id_rs1, Mux(EX2MEM0.io.MEMLoad_flag, MEM0.io.rdata, EX2MEM0.io.MEMwrb2reg),
    //                            Mux(CTRL0.io.feedflag_wb2id_rs1, Mux(MEM2WB0.io.WBLoad_flag, MEM2WB0.io.WBmemout, MEM2WB0.io.WBwrb2reg),
    //                            RegisterFiles0.io.regfile_out1)))
    ID2EX0.io.IDregout1     := Mux(CTRL0.io.feedflag_ex2id_rs1, ALU0.io.ALUout_data,
                               Mux(CTRL0.io.feedflag_mem2id_rs1, MEM2WB0.io.MEMwrb2reg,
                               Mux(CTRL0.io.feedflag_wb2id_rs1, MEM2WB0.io.WBwrb2reg,
                               RegisterFiles0.io.regfile_out1)))
    // ID2EX0.io.IDregout2     := Mux(CTRL0.io.feedflag_ex2id_rs2, ALU0.io.ALUout_data,
    //                            Mux(CTRL0.io.feedflag_mem2id_rs2, Mux(EX2MEM0.io.MEMLoad_flag, MEM0.io.rdata, EX2MEM0.io.MEMwrb2reg),
    //                            Mux(CTRL0.io.feedflag_wb2id_rs2, Mux(MEM2WB0.io.WBLoad_flag, MEM2WB0.io.WBmemout, MEM2WB0.io.WBwrb2reg),
    //                            RegisterFiles0.io.regfile_out2)))
    ID2EX0.io.IDregout2     := Mux(CTRL0.io.feedflag_ex2id_rs2, ALU0.io.ALUout_data,
                               Mux(CTRL0.io.feedflag_mem2id_rs2,MEM2WB0.io.MEMwrb2reg,
                               Mux(CTRL0.io.feedflag_wb2id_rs2, MEM2WB0.io.WBwrb2reg,
                               RegisterFiles0.io.regfile_out2)))
    ID2EX0.io.IDcsr_rd      := IDU0.io.csridx
    ID2EX0.io.IDcsrout      := CSR0.io.csr_out
    ID2EX0.io.IDecall_flag  := IDU0.io.ecall_flag
    ID2EX0.io.IDBtype_flag  := IDU0.io.Btype_flag
    ID2EX0.io.IDLoad_flag   := IDU0.io.Load_flag
    ID2EX0.io.IDpc          := IF2ID0.io.IDpc
    ID2EX0.io.IDinst        := IF2ID0.io.IDinst

    ALU0.io.regfile_out1 := ID2EX0.io.EXregout1
    ALU0.io.regfile_out2 := ID2EX0.io.EXregout2
    ALU0.io.csr_out      := ID2EX0.io.EXcsrout
    ALU0.io.csr_enw      := ID2EX0.io.EXcsr_enw
    ALU0.io.imm          := ID2EX0.io.EXimm
    ALU0.io.shamt        := ID2EX0.io.EXshamt
    ALU0.io.pc           := ID2EX0.io.EXpc
    ALU0.io.ALUctrl      := ID2EX0.io.EXALUctrl
    ALU0.io.Btype_flag   := ID2EX0.io.EXBtype_flag

    RegisterFiles0.io.clock     := clock
    RegisterFiles0.io.reset     := reset
    RegisterFiles0.io.read1_idx := IDU0.io.rs1
    RegisterFiles0.io.read2_idx := IDU0.io.rs2

    CSR0.io.clock     := clock
    CSR0.io.reset     := reset
    CSR0.io.read_idx  := IDU0.io.csridx
    //EX and MEM
    EX2MEM0.io.enEX2MEM   := !CTRL0.io.stall_ex2mem
    EX2MEM0.io.flush      := CTRL0.io.flush_ex2mem

    EX2MEM0.io.EXraddr    := ALU0.io.ALUout_data
    EX2MEM0.io.EXwaddr    := ALU0.io.ALUout_data
    EX2MEM0.io.EXwdata    := ID2EX0.io.EXregout2
    EX2MEM0.io.EXwmask    := ID2EX0.io.EXWmask
    EX2MEM0.io.EXLOADctrl := ID2EX0.io.EXLOADctrl
    EX2MEM0.io.EXrd       := ID2EX0.io.EXrd
    EX2MEM0.io.EXenw      := ID2EX0.io.EXenw
    EX2MEM0.io.EXcsr_enw  := ID2EX0.io.EXcsr_enw
    EX2MEM0.io.EXwrb2reg  := Mux(ID2EX0.io.EXcsr_enw === 0.U, ALU0.io.ALUout_data, ID2EX0.io.EXcsrout)
    EX2MEM0.io.EXcsr_rd   := ID2EX0.io.EXcsr_rd
    EX2MEM0.io.EXwrb2csr  := ALU0.io.ALUout_data
    EX2MEM0.io.EXecall_flag:=ID2EX0.io.EXecall_flag
    EX2MEM0.io.EXLoad_flag:= ID2EX0.io.EXLoad_flag
    EX2MEM0.io.EXpc       := ID2EX0.io.EXpc
    EX2MEM0.io.EXinst     := ID2EX0.io.EXinst

    MEM0.io.raddr    := EX2MEM0.io.MEMraddr
    MEM0.io.waddr    := EX2MEM0.io.MEMwaddr
    MEM0.io.wdata    := EX2MEM0.io.MEMwdata
    MEM0.io.wmask    := EX2MEM0.io.MEMwmask
    MEM0.io.LOADctrl := EX2MEM0.io.MEMLOADctrl

    //MEM and WB
    MEM2WB0.io.enMEM2WB    := !CTRL0.io.stall_mem2wb
    MEM2WB0.io.flush       := CTRL0.io.flush_mem2wb
 
    MEM2WB0.io.MEMrd       := EX2MEM0.io.MEMrd
    MEM2WB0.io.MEMenw      := EX2MEM0.io.MEMenw
    MEM2WB0.io.MEMcsr_enw  := EX2MEM0.io.MEMcsr_enw
    MEM2WB0.io.MEMwrb2reg  := Mux(MEM2WB0.io.MEMLoad_flag, MEM0.io.rdata, EX2MEM0.io.MEMwrb2reg)
    MEM2WB0.io.MEMcsr_rd   := EX2MEM0.io.MEMcsr_rd
    MEM2WB0.io.MEMwrb2csr  := EX2MEM0.io.MEMwrb2csr
    MEM2WB0.io.MEMmemout   := MEM0.io.rdata
    MEM2WB0.io.MEMLOADctrl := EX2MEM0.io.MEMLOADctrl
    MEM2WB0.io.MEMecall_flag:=EX2MEM0.io.MEMecall_flag
    MEM2WB0.io.MEMLoad_flag:= EX2MEM0.io.MEMLoad_flag
    MEM2WB0.io.MEMpc       := EX2MEM0.io.MEMpc
    MEM2WB0.io.MEMinst     := EX2MEM0.io.MEMinst

    RegisterFiles0.io.write_idx := MEM2WB0.io.WBrd
    RegisterFiles0.io.enw       := MEM2WB0.io.WBenw
    RegisterFiles0.io.in_data   := MEM2WB0.io.WBwrb2reg
    
    CSR0.io.write_idx := MEM2WB0.io.WBcsr_rd
    CSR0.io.enw     := MEM2WB0.io.WBcsr_enw
    CSR0.io.in_data := MEM2WB0.io.WBwrb2csr

    CSR0.io.ecall_flag   := MEM2WB0.io.WBecall_flag
    CSR0.io.ecall_mepc   := MEM2WB0.io.WBpc
    CSR0.io.ecall_mcause := "hb".U
    
    //CTRL
    CTRL0.io.id_rs1         := IDU0.io.rs1
    CTRL0.io.id_rs2         := IDU0.io.rs2
    CTRL0.io.ex_rd          := ID2EX0.io.EXrd
    CTRL0.io.ex_enw         := ID2EX0.io.EXenw
    CTRL0.io.mem_rd         := EX2MEM0.io.MEMrd
    CTRL0.io.mem_enw        := EX2MEM0.io.MEMenw
    CTRL0.io.wb_rd          := MEM2WB0.io.WBrd
    CTRL0.io.wb_enw         := MEM2WB0.io.WBenw
    CTRL0.io.flushreq_id    := IDU0.io.flush_req
    CTRL0.io.flushreq_ex    := ALU0.io.flush_req
    CTRL0.io.flushreq_ecall := MEM2WB0.io.WBecall_flag
    CTRL0.io.loadflag_ex    := ID2EX0.io.EXLoad_flag

    //top input
    io.pc   := MEM2WB0.io.WBpc
    io.inst := MEM2WB0.io.WBinst
}