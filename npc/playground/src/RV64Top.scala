import chisel3._
import chisel3.util._
import xbqpackage._

// pipline
class RV64Top extends Module
{
    val io = IO(new Bundle
    {
        val pc = Output(UInt(64.W))
        val inst = Output(UInt(32.W))

        val mem_pc = Output(UInt(64.W))
        val mem_addr = Output(UInt(64.W))
        val mem_wdata = Output(UInt(64.W))
        val enMEM2WB = Output(Bool())
        
        // SOC io_interrupt
        val interrupt = Input(UInt(1.W))

        // // TopInterface
        // val mode = Output(UInt(1.W)) // mode=0 -> read; mode -> 1 write;
        // val valid = Output(Bool())
        // val addr = Output(UInt(64.W))
        // val w_data = Output(UInt(8.W))
        // val ready = Input(Bool())
        // val r_data_valid = Input(Bool())
        // val r_data = Input(UInt(8.W))

        // // SRAM
        // val sram0_addr      = Output(UInt(6.W))
        // val sram0_cen       = Output(UInt(1.W))
        // val sram0_wen       = Output(UInt(1.W))
        // val sram0_wmask     = Output(UInt(128.W))
        // val sram0_wdata     = Output(UInt(128.W))
        // val sram0_rdata     = Input(UInt(128.W))

        // val sram1_addr      = Output(UInt(6.W))
        // val sram1_cen       = Output(UInt(1.W))
        // val sram1_wen       = Output(UInt(1.W))
        // val sram1_wmask     = Output(UInt(128.W))
        // val sram1_wdata     = Output(UInt(128.W))
        // val sram1_rdata     = Input(UInt(128.W))

        // val sram2_addr      = Output(UInt(6.W))
        // val sram2_cen       = Output(UInt(1.W))
        // val sram2_wen       = Output(UInt(1.W))
        // val sram2_wmask     = Output(UInt(128.W))
        // val sram2_wdata     = Output(UInt(128.W))
        // val sram2_rdata     = Input(UInt(128.W))

        // val sram3_addr      = Output(UInt(6.W))
        // val sram3_cen       = Output(UInt(1.W))
        // val sram3_wen       = Output(UInt(1.W))
        // val sram3_wmask     = Output(UInt(128.W))
        // val sram3_wdata     = Output(UInt(128.W))
        // val sram3_rdata     = Input(UInt(128.W))

        // val sram4_addr      = Output(UInt(6.W))
        // val sram4_cen       = Output(UInt(1.W))
        // val sram4_wen       = Output(UInt(1.W))
        // val sram4_wmask     = Output(UInt(128.W))
        // val sram4_wdata     = Output(UInt(128.W))
        // val sram4_rdata     = Input(UInt(128.W))

        // val sram5_addr      = Output(UInt(6.W))
        // val sram5_cen       = Output(UInt(1.W))
        // val sram5_wen       = Output(UInt(1.W))
        // val sram5_wmask     = Output(UInt(128.W))
        // val sram5_wdata     = Output(UInt(128.W))
        // val sram5_rdata     = Input(UInt(128.W))

        // val sram6_addr      = Output(UInt(6.W))
        // val sram6_cen       = Output(UInt(1.W))
        // val sram6_wen       = Output(UInt(1.W))
        // val sram6_wmask     = Output(UInt(128.W))
        // val sram6_wdata     = Output(UInt(128.W))
        // val sram6_rdata     = Input(UInt(128.W))

        // val sram7_addr      = Output(UInt(6.W))
        // val sram7_cen       = Output(UInt(1.W))
        // val sram7_wen       = Output(UInt(1.W))
        // val sram7_wmask     = Output(UInt(128.W))
        // val sram7_wdata     = Output(UInt(128.W))
        // val sram7_rdata     = Input(UInt(128.W))
    })
    val IFU0 = Module(new IFU())
    // val IFU_DPI0 = Module(new ysyx_040154_IFU_DPI()) // verilog
    val IF2ID0 = Module(new IF2ID())
    val IDU0 = Module(new IDU())
    val ID2EX0 = Module(new ID2EX())
    val ALU0 = Module(new ALU())
    val RegisterFiles0 = Module(new ysyx_040154_RegisterFiles()) // verilog
    val EX2MEM0 = Module(new EX2MEM())
    // val MEM0 = Module(new MEM())
    val MEMCTRL0 = Module(new MEMCTRL())
    // val MEM_DPI0 = Module(new ysyx_040154_MEM_DPI()) // verilog
    val LOADUNIT0 = Module(new ysyx_040154_LOADUNIT()) // verilog
    val MEM2WB0 = Module(new MEM2WB())
    val CTRL0 = Module(new CTRL())
    val CSR0 = Module(new ysyx_040154_CSR()) // verilog
    val CLINT0 = Module(new CLINT())
    val ICACHE_CTRL0 = Module(new CACHE_CTRL())
    val ICACHE0 = Module(new ICACHE())
    val DCACHE_CTRL0 = Module(new DCACHE_CTRL())
    val DCACHE0 = Module(new DCACHE())
    val TopInterface0 = Module(new TopInterface())
    // val AXIRW0 = Module(new ysyx_040154_axi_rw()) // verilog
    val AXI_ARIBITER0 = Module(new AXI_ARIBITER()) // verilog
    val main_memory0 = Module(new main_memory())

    //IF and ID
    // IFU0.io.regfile_out1 := RegisterFiles0.io.regfile_out1
    // IFU0.io.IFUctrl      := IDU0.io.IFUctrl
    // IFU0.io.ALUout_data  := ALU0.io.ALUout_data(0)
    // IFU0.io.imm          := IDU0.io.imm
    IFU0.io.jump_flag := IDU0.io.jump_flag || ALU0.io.jump_flag || CLINT0.io.int_jump_flag
    // IFU0.io.pc_next   := Mux(MEM2WB0.io.WBclint_enw, CSR0.io.mtvec_out,
    //                      Mux(ALU0.io.jump_flag, ALU0.io.pc_next, 
    //                      Mux(IDU0.io.jump_flag, IDU0.io.pc_next, 0.U))) // check the priority, later is higher
    IFU0.io.pc_next   := Mux(ALU0.io.jump_flag, ALU0.io.pc_next, 
                         Mux(IDU0.io.jump_flag, IDU0.io.pc_next, 
                         Mux(CLINT0.io.int_jump_flag, CLINT0.io.int_jump_add, 0.U)))
                         // 存在数据冒险的风险，没有解决
    IFU0.io.enIFU     := !CTRL0.io.stall_ifu
    IFU0.io.data      := ICACHE_CTRL0.io.data2cpu
    IFU0.io.ready     := ICACHE_CTRL0.io.ready2cpu
    // IFU_DPI0.io.pc    := ICACHE_CTRL0.io.addr2mem // DPI

    // TopInterface0 input
    TopInterface0.io.clock  := clock
    TopInterface0.io.reset  := reset
    TopInterface0.io.rw_valid_i     := AXI_ARIBITER0.io.rw_valid_i
    TopInterface0.io.enw_i          := AXI_ARIBITER0.io.enw_i
    TopInterface0.io.rw_w_data_i    := AXI_ARIBITER0.io.rw_w_data_i
    TopInterface0.io.rw_addr_i      := AXI_ARIBITER0.io.rw_addr_i
    TopInterface0.io.rw_size_i      := AXI_ARIBITER0.io.rw_size_i

    // TopInterface0.io.ready   := io.ready
    // TopInterface0.io.r_data_valid   := io.r_data_valid
    // TopInterface0.io.r_data         := io.r_data
    TopInterface0.io.ready          := main_memory0.io.ready
    TopInterface0.io.r_data_valid   := main_memory0.io.r_data_valid
    TopInterface0.io.r_data         := main_memory0.io.r_data

    // AXIRW0.io.clock         := clock // AXI
    // AXIRW0.io.reset         := reset // AXI
    // // AXIRW0.io.rw_addr_i     := ICACHE_CTRL0.io.addr2mem(31, 0) // AXI
    // // AXIRW0.io.rw_valid_i    := ICACHE_CTRL0.io.valid2mem // AXI
    // // AXIRW0.io.enw_i         := ICACHE_CTRL0.io.enw2mem // AXI
    // // AXIRW0.io.rw_w_data_i   := ICACHE_CTRL0.io.data2mem // AXI
    // // AXIRW0.io.rw_size_i     := ICACHE_CTRL0.io.wmask2mem // AXI
    // AXIRW0.io.rw_addr_i     := AXI_ARIBITER0.io.rw_addr_i // AXI
    // AXIRW0.io.rw_valid_i    := AXI_ARIBITER0.io.rw_valid_i // AXI
    // AXIRW0.io.enw_i         := AXI_ARIBITER0.io.enw_i // AXI
    // AXIRW0.io.rw_w_data_i   := AXI_ARIBITER0.io.rw_w_data_i // AXI
    // AXIRW0.io.rw_size_i     := AXI_ARIBITER0.io.rw_size_i // AXI

    // AXIRW0.io.axi_aw_ready_i:= io.master_awready // AXI
    // AXIRW0.io.axi_w_ready_i := io.master_wready // AXI
    // AXIRW0.io.axi_b_valid_i := io.master_bvalid // AXI 
    // AXIRW0.io.axi_b_resp_i  := io.master_bresp // AXI
    // AXIRW0.io.axi_b_id_i    := io.master_bid // AXI
    // AXIRW0.io.axi_b_user_i  := 0.U // AXI
    // AXIRW0.io.axi_ar_ready_i:= io.master_arready // AXI
    // AXIRW0.io.axi_r_valid_i := io.master_rvalid // AXI
    // AXIRW0.io.axi_r_resp_i  := io.master_rresp // AXI
    // AXIRW0.io.axi_r_data_i  := io.master_rdata // AXI
    // AXIRW0.io.axi_r_last_i  := io.master_rlast // AXI
    // AXIRW0.io.axi_r_id_i    := io.master_rid // AXI
    // AXIRW0.io.axi_r_user_i  := 0.U // AXI

    IF2ID0.io.enIF2ID := !CTRL0.io.stall_if2id
    IF2ID0.io.flush   := CTRL0.io.flush_if2id

    IF2ID0.io.IFpc    := IFU0.io.pc
    // IF2ID0.io.IFinst  := IFU_DPI0.io.inst
    IF2ID0.io.IFinst  := IFU0.io.inst

    IDU0.io.pc   := IF2ID0.io.IDpc
    IDU0.io.inst := IF2ID0.io.IDinst
    IDU0.io.regfile_out1 := ID2EX0.io.IDregout1
    IDU0.io.mtvec_out := CSR0.io.mtvec_out
    IDU0.io.mepc_out := CSR0.io.mepc_out

    ICACHE_CTRL0.io.cpu_addr := IFU0.io.pc
    ICACHE_CTRL0.io.cpu_data := 0.U
    ICACHE_CTRL0.io.cpu_enw := false.B
    ICACHE_CTRL0.io.cpu_wmask := 0.U
    ICACHE_CTRL0.io.cpu_valid := IFU0.io.pc2cache_valid

    // ICACHE_CTRL0.io.mem_data := IFU_DPI0.io.raw_data // DPI
    // ICACHE_CTRL0.io.mem_ready := true.B // DPI
    // ICACHE_CTRL0.io.mem_data    := AXIRW0.io.data_read_o// AXI
    // ICACHE_CTRL0.io.mem_ready   := AXIRW0.io.rw_ready_o // AXI
    ICACHE_CTRL0.io.mem_data    := AXI_ARIBITER0.io.ICache_mem_data// AXI
    ICACHE_CTRL0.io.mem_ready   := AXI_ARIBITER0.io.ICache_mem_ready // AXI

    ICACHE_CTRL0.io.cache_data := ICACHE0.io.data
    ICACHE_CTRL0.io.cache_valid := ICACHE0.io.valid
    ICACHE_CTRL0.io.cache_dirty := ICACHE0.io.dirty
    ICACHE_CTRL0.io.cache_tag := ICACHE0.io.tag

    ICACHE0.io.CLK := clock
    ICACHE0.io.index := ICACHE_CTRL0.io.index2cache
    ICACHE0.io.enw := ICACHE_CTRL0.io.enw2cache
    ICACHE0.io.tag_enw := ICACHE_CTRL0.io.tagenw2cache
    ICACHE0.io.wdata := ICACHE_CTRL0.io.wdata2cache
    ICACHE0.io.in_valid := ICACHE_CTRL0.io.valid2cache
    ICACHE0.io.in_dirty := ICACHE_CTRL0.io.dirty2cache
    ICACHE0.io.in_tag := ICACHE_CTRL0.io.tag2cache
    // ICACHE0.io.sram0_rdata := io.sram0_rdata
    // ICACHE0.io.sram1_rdata := io.sram1_rdata
    // ICACHE0.io.sram2_rdata := io.sram2_rdata
    // ICACHE0.io.sram3_rdata := io.sram3_rdata
    //ID and EX
    ID2EX0.io.enID2EX   := !CTRL0.io.stall_id2ex
    ID2EX0.io.flush     := CTRL0.io.flush_id2ex
    ID2EX0.io.async_int_flag     := CLINT0.io.async_int_flag

    ID2EX0.io.IDimm         := IDU0.io.imm
    ID2EX0.io.IDzimm        := IDU0.io.zimm
    ID2EX0.io.IDshamt       := IDU0.io.shamt
    ID2EX0.io.IDALUctrl     := IDU0.io.ALUctrl
    // ID2EX0.io.IDIFUctrl     := IDU0.io.IFUctrl
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
    ID2EX0.io.IDcsr_rd          := IDU0.io.csridx
    ID2EX0.io.IDcsrout          := CSR0.io.csr_out
    ID2EX0.io.IDclint_enw       := CLINT0.io.csr_enw
    ID2EX0.io.IDclint_mstatus   := CLINT0.io.mstatus_out
    ID2EX0.io.IDclint_mepc      := CLINT0.io.mepc_out
    ID2EX0.io.IDclint_mcause    := CLINT0.io.mcause_out
    ID2EX0.io.IDclint_mip       := CLINT0.io.mip_out
    ID2EX0.io.IDdiv_flag        := IDU0.io.div_flag
    ID2EX0.io.IDdiv_signed      := IDU0.io.div_signed
    ID2EX0.io.IDmul_signed      := IDU0.io.mul_signed
    ID2EX0.io.IDmul_outh        := IDU0.io.mul_outh
    ID2EX0.io.IDmul_flag        := IDU0.io.mul_flag
    ID2EX0.io.IDBtype_flag      := IDU0.io.Btype_flag
    ID2EX0.io.IDLoad_flag       := IDU0.io.Load_flag
    ID2EX0.io.IDfencei_flag     := IDU0.io.fencei_flag
    ID2EX0.io.IDpc              := IF2ID0.io.IDpc
    ID2EX0.io.IDinst            := IF2ID0.io.IDinst

    ALU0.io.clock        := clock
    ALU0.io.reset        := reset
    ALU0.io.regfile_out1 := ID2EX0.io.EXregout1
    ALU0.io.regfile_out2 := ID2EX0.io.EXregout2
    ALU0.io.csr_out      := ID2EX0.io.EXcsrout
    ALU0.io.csr_enw      := ID2EX0.io.EXcsr_enw
    ALU0.io.imm          := ID2EX0.io.EXimm
    ALU0.io.zimm         := ID2EX0.io.EXzimm
    ALU0.io.shamt        := ID2EX0.io.EXshamt
    ALU0.io.pc           := ID2EX0.io.EXpc
    ALU0.io.ALUctrl      := ID2EX0.io.EXALUctrl
    ALU0.io.div_flag     := ID2EX0.io.EXdiv_flag
    ALU0.io.div_signed   := ID2EX0.io.EXdiv_signed
    ALU0.io.mul_flag     := ID2EX0.io.EXmul_flag
    ALU0.io.mul_signed   := ID2EX0.io.EXmul_signed
    ALU0.io.mul_outh     := ID2EX0.io.EXmul_outh
    ALU0.io.Btype_flag   := ID2EX0.io.EXBtype_flag
    ALU0.io.out_take_ready := !CTRL0.io.stall_id2ex

    RegisterFiles0.io.clock     := clock
    RegisterFiles0.io.reset     := reset
    RegisterFiles0.io.read1_idx := IDU0.io.rs1
    RegisterFiles0.io.read2_idx := IDU0.io.rs2

    CSR0.io.clock     := clock
    CSR0.io.reset     := reset
    CSR0.io.read_idx  := IDU0.io.csridx

    CLINT0.io.inst          := IF2ID0.io.IDinst
    CLINT0.io.IFpc          := IF2ID0.io.IFpc
    CLINT0.io.IDpc          := IF2ID0.io.IDpc
    CLINT0.io.global_int_en := CSR0.io.global_int_en
    CLINT0.io.int_flag      := io.interrupt
    CLINT0.io.mstatus_in    := CSR0.io.mstatus_out
    CLINT0.io.mepc_in       := CSR0.io.mepc_out
    CLINT0.io.mtvec_in      := CSR0.io.mtvec_out
    CLINT0.io.mcause_in     := CSR0.io.mcause_out
    CLINT0.io.mip_in        := CSR0.io.mip_out
    CLINT0.io.mie_in        := CSR0.io.mie_out
    CLINT0.io.flushreq_ex   := ALU0.io.flush_req
    CLINT0.io.EXpc         := ID2EX0.io.EXpc

    //EX and MEM
    EX2MEM0.io.enEX2MEM         := !CTRL0.io.stall_ex2mem
    EX2MEM0.io.flush            := CTRL0.io.flush_ex2mem

    EX2MEM0.io.EXraddr          := ALU0.io.ALUout_data
    EX2MEM0.io.EXwaddr          := ALU0.io.ALUout_data
    EX2MEM0.io.EXwdata          := ID2EX0.io.EXregout2
    EX2MEM0.io.EXwmask          := ID2EX0.io.EXWmask
    EX2MEM0.io.EXLOADctrl       := ID2EX0.io.EXLOADctrl
    EX2MEM0.io.EXrd             := ID2EX0.io.EXrd
    EX2MEM0.io.EXenw            := ID2EX0.io.EXenw
    EX2MEM0.io.EXcsr_enw        := ID2EX0.io.EXcsr_enw
    EX2MEM0.io.EXwrb2reg        := Mux(ID2EX0.io.EXcsr_enw === 0.U, ALU0.io.ALUout_data, ID2EX0.io.EXcsrout)
    EX2MEM0.io.EXcsr_rd         := ID2EX0.io.EXcsr_rd
    EX2MEM0.io.EXwrb2csr        := ALU0.io.ALUout_data
    EX2MEM0.io.EXclint_enw      := ID2EX0.io.EXclint_enw
    EX2MEM0.io.EXclint_mstatus  := ID2EX0.io.EXclint_mstatus
    EX2MEM0.io.EXclint_mepc     := ID2EX0.io.EXclint_mepc  
    EX2MEM0.io.EXclint_mcause   := ID2EX0.io.EXclint_mcause
    EX2MEM0.io.EXclint_mip      := ID2EX0.io.EXclint_mip
    EX2MEM0.io.EXLoad_flag      := ID2EX0.io.EXLoad_flag
    EX2MEM0.io.EXfencei_flag    := ID2EX0.io.EXfencei_flag
    EX2MEM0.io.EXpc             := ID2EX0.io.EXpc
    EX2MEM0.io.EXinst           := ID2EX0.io.EXinst

    // MEM0.io.raddr    := EX2MEM0.io.MEMraddr
    // MEM0.io.waddr    := EX2MEM0.io.MEMwaddr
    // MEM0.io.wdata    := EX2MEM0.io.MEMwdata
    // MEM0.io.wmask    := EX2MEM0.io.MEMwmask
    // MEM0.io.enMEM    := MEM2WB0.io.enMEM2WB
    // MEM0.io.LOADctrl := EX2MEM0.io.MEMLOADctrl

    DCACHE_CTRL0.io.cpu_addr    := EX2MEM0.io.MEMraddr // raddr is the same with waddr
    DCACHE_CTRL0.io.cpu_data    := EX2MEM0.io.MEMwdata
    DCACHE_CTRL0.io.cpu_enw     := (EX2MEM0.io.MEMwmask =/= "h00".U(8.W))
    DCACHE_CTRL0.io.cpu_wmask   := EX2MEM0.io.MEMwmask
    DCACHE_CTRL0.io.cpu_valid   := MEMCTRL0.io.dcache_valid
    DCACHE_CTRL0.io.fencei_flag := EX2MEM0.io.MEMfencei_flag
    // DCACHE_CTRL0.io.uncached_flag := MEMCTRL0.io.uncached_flag

    // DCACHE_CTRL0.io.mem_data := MEM_DPI0.io.rdata
    // DCACHE_CTRL0.io.mem_ready := true.B //!!!!!
    DCACHE_CTRL0.io.mem_data    := AXI_ARIBITER0.io.DCache_mem_data
    DCACHE_CTRL0.io.mem_ready   := AXI_ARIBITER0.io.DCache_mem_ready

    DCACHE_CTRL0.io.cache_data  := DCACHE0.io.data
    DCACHE_CTRL0.io.cache_valid := DCACHE0.io.valid
    DCACHE_CTRL0.io.cache_dirty := DCACHE0.io.dirty
    DCACHE_CTRL0.io.cache_tag   := DCACHE0.io.tag

    MEMCTRL0.io.loadstore_flag  := ((EX2MEM0.io.MEMLOADctrl =/= "b000".U(3.W)) || (EX2MEM0.io.MEMwmask =/= "h00".U(8.W)))
    MEMCTRL0.io.dcache_ready    := DCACHE_CTRL0.io.ready2cpu
    MEMCTRL0.io.fencei_flag     := EX2MEM0.io.MEMfencei_flag
    MEMCTRL0.io.pc      := EX2MEM0.io.MEMpc
    MEMCTRL0.io.addr    := EX2MEM0.io.MEMraddr  

    DCACHE0.io.CLK      := clock
    DCACHE0.io.index    := DCACHE_CTRL0.io.index2cache
    DCACHE0.io.enw      := DCACHE_CTRL0.io.enw2cache
    DCACHE0.io.tag_enw  := DCACHE_CTRL0.io.tagenw2cache
    DCACHE0.io.wdata    := DCACHE_CTRL0.io.wdata2cache
    DCACHE0.io.wmask    := DCACHE_CTRL0.io.wmask2cache
    DCACHE0.io.in_valid := DCACHE_CTRL0.io.valid2cache
    DCACHE0.io.in_dirty := DCACHE_CTRL0.io.dirty2cache
    DCACHE0.io.in_tag   := DCACHE_CTRL0.io.tag2cache
    // DCACHE0.io.sram4_rdata := io.sram4_rdata
    // DCACHE0.io.sram5_rdata := io.sram5_rdata
    // DCACHE0.io.sram6_rdata := io.sram6_rdata
    // DCACHE0.io.sram7_rdata := io.sram7_rdata
    // MEM_DPI0.io.clock := clock
    // MEM_DPI0.io.raddr := Mux(DCACHE_CTRL0.io.uart_dpi_flag, DCACHE_CTRL0.io.addr2mem, 0.U) // DPI-C
    // MEM_DPI0.io.waddr := Mux(DCACHE_CTRL0.io.uart_dpi_flag, DCACHE_CTRL0.io.addr2mem, 0.U) // DPI-C
    // MEM_DPI0.io.raddr := DCACHE_CTRL0.io.addr2mem // DPI-C
    // MEM_DPI0.io.waddr := DCACHE_CTRL0.io.addr2mem // DPI-C
    // MEM_DPI0.io.wdata := DCACHE_CTRL0.io.data2mem // DPI-C
    // MEM_DPI0.io.wmask := DCACHE_CTRL0.io.wmask2mem // DPI-C

    LOADUNIT0.io.raddr          := EX2MEM0.io.MEMraddr(2, 0)
    // LOADUNIT0.io.rdata_native   := Mux(EX2MEM0.io.MEMraddr === "ha0000048".U, MEM_DPI0.io.rdata, DCACHE_CTRL0.io.data2cpu)
    LOADUNIT0.io.rdata_native   := DCACHE_CTRL0.io.data2cpu
    LOADUNIT0.io.LOADctrl       := EX2MEM0.io.MEMLOADctrl

    CLINT0.io.mtimecmp_enw  := MEMCTRL0.io.mtimecmp_flag && (EX2MEM0.io.MEMwmask =/= 0.U)
    CLINT0.io.mtimecmp_in   := EX2MEM0.io.MEMwdata
    CLINT0.io.mtime_enw     := MEMCTRL0.io.mtime_flag && (EX2MEM0.io.MEMwmask =/= 0.U)
    CLINT0.io.mtime_in      := EX2MEM0.io.MEMwdata
    //MEM and WB
    MEM2WB0.io.enMEM2WB    := !CTRL0.io.stall_mem2wb
    MEM2WB0.io.flush       := CTRL0.io.flush_mem2wb
 
    MEM2WB0.io.MEMrd       := EX2MEM0.io.MEMrd
    MEM2WB0.io.MEMenw      := EX2MEM0.io.MEMenw
    MEM2WB0.io.MEMcsr_enw  := EX2MEM0.io.MEMcsr_enw
    // MEM2WB0.io.MEMwrb2reg  := Mux(MEM2WB0.io.MEMLoad_flag, MEM0.io.rdata, EX2MEM0.io.MEMwrb2reg)
    MEM2WB0.io.MEMwrb2reg  := Mux(EX2MEM0.io.MEMLoad_flag, 
                              Mux(MEMCTRL0.io.mtimecmp_flag, CLINT0.io.mtimecmp_out, 
                              Mux(MEMCTRL0.io.mtime_flag, CLINT0.io.mtime_out, LOADUNIT0.io.rdata)), 
                              EX2MEM0.io.MEMwrb2reg)
    
    MEM2WB0.io.MEMcsr_rd   := EX2MEM0.io.MEMcsr_rd
    MEM2WB0.io.MEMwrb2csr  := EX2MEM0.io.MEMwrb2csr
    // MEM2WB0.io.MEMmemout   := MEM0.io.rdata
    MEM2WB0.io.MEMmemout   := LOADUNIT0.io.rdata // !!!

    MEM2WB0.io.MEMLOADctrl := EX2MEM0.io.MEMLOADctrl
    MEM2WB0.io.MEMwaddr    := EX2MEM0.io.MEMwaddr
    MEM2WB0.io.MEMclint_enw:= EX2MEM0.io.MEMclint_enw
    MEM2WB0.io.MEMclint_mstatus:=  EX2MEM0.io.MEMclint_mstatus
    MEM2WB0.io.MEMclint_mepc   :=  EX2MEM0.io.MEMclint_mepc  
    MEM2WB0.io.MEMclint_mcause :=  EX2MEM0.io.MEMclint_mcause
    MEM2WB0.io.MEMclint_mip    :=  EX2MEM0.io.MEMclint_mip
    // MEM2WB0.io.MEMLoad_flag:= EX2MEM0.io.MEMLoad_flag
    MEM2WB0.io.MEMpc       := EX2MEM0.io.MEMpc
    MEM2WB0.io.MEMinst     := EX2MEM0.io.MEMinst

    RegisterFiles0.io.write_idx := MEM2WB0.io.WBrd
    //注意Reg的写回信号要加入流水线是否暂停的判断，否则会导致difftest错误
    RegisterFiles0.io.enw       := !CTRL0.io.stall_mem2wb && (MEM2WB0.io.WBenw === 1.U)
    RegisterFiles0.io.in_data   := MEM2WB0.io.WBwrb2reg
    
    CSR0.io.write_idx := MEM2WB0.io.WBcsr_rd
    CSR0.io.enw     := (MEM2WB0.io.WBcsr_enw === 1.U) && !CTRL0.io.stall_mem2wb
    CSR0.io.in_data := MEM2WB0.io.WBwrb2csr

    CSR0.io.clint_enw   := (MEM2WB0.io.WBclint_enw === 1.U) && !CTRL0.io.stall_mem2wb
    CSR0.io.mstatus_in  := MEM2WB0.io.WBclint_mstatus
    CSR0.io.mepc_in     := MEM2WB0.io.WBclint_mepc
    CSR0.io.mcause_in   := MEM2WB0.io.WBclint_mcause
    CSR0.io.mip_in      := MEM2WB0.io.WBclint_mip

    //CTRL
    CTRL0.io.id_rs1         := IDU0.io.rs1
    CTRL0.io.id_rs2         := IDU0.io.rs2
    CTRL0.io.ex_rd          := ID2EX0.io.EXrd
    CTRL0.io.ex_enw         := ID2EX0.io.EXenw
    CTRL0.io.mem_rd         := EX2MEM0.io.MEMrd
    CTRL0.io.mem_enw        := EX2MEM0.io.MEMenw
    CTRL0.io.wb_rd          := MEM2WB0.io.WBrd
    CTRL0.io.wb_enw         := MEM2WB0.io.WBenw
    CTRL0.io.flushreq_id    := IDU0.io.flush_req || CLINT0.io.int_jump_flag // CLINT的pc判断在ID处, 所以也要冲刷IF2ID
    CTRL0.io.flushreq_ex    := ALU0.io.flush_req
    CTRL0.io.async_int_flag     := CLINT0.io.async_int_flag
    CTRL0.io.ifu_stall_req      := IFU0.io.ifu_stall_req
    CTRL0.io.dcache_stall_req   := MEMCTRL0.io.memstall_req
    CTRL0.io.loadflag_ex    := ID2EX0.io.EXLoad_flag
    CTRL0.io.mulstall_req   := ALU0.io.mulstall_req
    CTRL0.io.divstall_req   := ALU0.io.divstall_req

    //AXI Aribter
    AXI_ARIBITER0.io.ICache_addr2mem    := ICACHE_CTRL0.io.addr2mem(31, 0)
    AXI_ARIBITER0.io.ICache_data2mem    := ICACHE_CTRL0.io.data2mem
    AXI_ARIBITER0.io.ICache_wmask2mem   := ICACHE_CTRL0.io.wmask2mem
    AXI_ARIBITER0.io.ICache_valid2mem   := ICACHE_CTRL0.io.valid2mem
    AXI_ARIBITER0.io.ICache_enw2mem     := ICACHE_CTRL0.io.enw2mem
    
    AXI_ARIBITER0.io.DCache_addr2mem    := DCACHE_CTRL0.io.addr2mem(31, 0)
    AXI_ARIBITER0.io.DCache_data2mem    := DCACHE_CTRL0.io.data2mem
    AXI_ARIBITER0.io.DCache_wmask2mem   := DCACHE_CTRL0.io.wmask2mem
    AXI_ARIBITER0.io.DCache_valid2mem   := DCACHE_CTRL0.io.valid2mem
    AXI_ARIBITER0.io.DCache_enw2mem     := DCACHE_CTRL0.io.enw2mem

    // AXI_ARIBITER0.io.rw_ready_o         := AXIRW0.io.rw_ready_o
    // AXI_ARIBITER0.io.data_read_o        := AXIRW0.io.data_read_o
    AXI_ARIBITER0.io.rw_ready_o         := TopInterface0.io.rw_ready_o
    AXI_ARIBITER0.io.data_read_o        := TopInterface0.io.data_read_o

    //top input
    io.pc           := MEM2WB0.io.WBpc
    io.inst         := MEM2WB0.io.WBinst
    io.mem_pc       := EX2MEM0.io.MEMpc
    io.mem_addr     := EX2MEM0.io.MEMwaddr
    io.mem_wdata    := EX2MEM0.io.MEMwdata
    io.enMEM2WB     := MEM2WB0.io.enMEM2WB

    // io.mode     := TopInterface0.io.mode
    // io.valid    := TopInterface0.io.valid
    // io.addr     := TopInterface0.io.addr
    // io.w_data   := TopInterface0.io.w_data

    main_memory0.io.clock    := clock
    main_memory0.io.mode     := TopInterface0.io.mode
    main_memory0.io.valid    := TopInterface0.io.valid
    main_memory0.io.addr     := TopInterface0.io.addr
    main_memory0.io.w_data   := TopInterface0.io.w_data
    // // SRAM
    // io.sram0_addr   := ICACHE0.io.sram0_addr 
    // io.sram0_cen    := ICACHE0.io.sram0_cen  
    // io.sram0_wen    := ICACHE0.io.sram0_wen  
    // io.sram0_wmask  := ICACHE0.io.sram0_wmask
    // io.sram0_wdata  := ICACHE0.io.sram0_wdata

    // io.sram1_addr   := ICACHE0.io.sram1_addr 
    // io.sram1_cen    := ICACHE0.io.sram1_cen  
    // io.sram1_wen    := ICACHE0.io.sram1_wen  
    // io.sram1_wmask  := ICACHE0.io.sram1_wmask
    // io.sram1_wdata  := ICACHE0.io.sram1_wdata

    // io.sram2_addr   := ICACHE0.io.sram2_addr 
    // io.sram2_cen    := ICACHE0.io.sram2_cen  
    // io.sram2_wen    := ICACHE0.io.sram2_wen  
    // io.sram2_wmask  := ICACHE0.io.sram2_wmask
    // io.sram2_wdata  := ICACHE0.io.sram2_wdata

    // io.sram3_addr   := ICACHE0.io.sram3_addr 
    // io.sram3_cen    := ICACHE0.io.sram3_cen  
    // io.sram3_wen    := ICACHE0.io.sram3_wen  
    // io.sram3_wmask  := ICACHE0.io.sram3_wmask
    // io.sram3_wdata  := ICACHE0.io.sram3_wdata

    // io.sram4_addr   := DCACHE0.io.sram4_addr 
    // io.sram4_cen    := DCACHE0.io.sram4_cen  
    // io.sram4_wen    := DCACHE0.io.sram4_wen  
    // io.sram4_wmask  := DCACHE0.io.sram4_wmask
    // io.sram4_wdata  := DCACHE0.io.sram4_wdata

    // io.sram5_addr   := DCACHE0.io.sram5_addr 
    // io.sram5_cen    := DCACHE0.io.sram5_cen  
    // io.sram5_wen    := DCACHE0.io.sram5_wen  
    // io.sram5_wmask  := DCACHE0.io.sram5_wmask
    // io.sram5_wdata  := DCACHE0.io.sram5_wdata

    // io.sram6_addr   := DCACHE0.io.sram6_addr 
    // io.sram6_cen    := DCACHE0.io.sram6_cen  
    // io.sram6_wen    := DCACHE0.io.sram6_wen  
    // io.sram6_wmask  := DCACHE0.io.sram6_wmask
    // io.sram6_wdata  := DCACHE0.io.sram6_wdata

    // io.sram7_addr   := DCACHE0.io.sram7_addr 
    // io.sram7_cen    := DCACHE0.io.sram7_cen  
    // io.sram7_wen    := DCACHE0.io.sram7_wen  
    // io.sram7_wmask  := DCACHE0.io.sram7_wmask
    // io.sram7_wdata  := DCACHE0.io.sram7_wdata

// io.axi_aw_valid_o   := AXIRW0.io.axi_aw_valid_o 
// io.axi_aw_addr_o    := AXIRW0.io.axi_aw_addr_o  
// io.axi_aw_prot_o    := AXIRW0.io.axi_aw_prot_o  
// io.axi_aw_id_o      := AXIRW0.io.axi_aw_id_o    
// io.axi_aw_user_o    := AXIRW0.io.axi_aw_user_o  
// io.axi_aw_len_o     := AXIRW0.io.axi_aw_len_o   
// io.axi_aw_size_o    := AXIRW0.io.axi_aw_size_o  
// io.axi_aw_burst_o   := AXIRW0.io.axi_aw_burst_o 
// io.axi_aw_lock_o    := AXIRW0.io.axi_aw_lock_o  
// io.axi_aw_cache_o   := AXIRW0.io.axi_aw_cache_o 
// io.axi_aw_qos_o     := AXIRW0.io.axi_aw_qos_o   
// io.axi_aw_region_o  := AXIRW0.io.axi_aw_region_o

// io.axi_w_valid_o    := AXIRW0.io.axi_w_valid_o  
// io.axi_w_data_o     := AXIRW0.io.axi_w_data_o   
// io.axi_w_strb_o     := AXIRW0.io.axi_w_strb_o   
// io.axi_w_last_o     := AXIRW0.io.axi_w_last_o   
// io.axi_w_user_o     := AXIRW0.io.axi_w_user_o   

// io.axi_b_ready_o    := AXIRW0.io.axi_b_ready_o  

// io.axi_ar_valid_o   := AXIRW0.io.axi_ar_valid_o 
// io.axi_ar_addr_o    := AXIRW0.io.axi_ar_addr_o  
// io.axi_ar_prot_o    := AXIRW0.io.axi_ar_prot_o  
// io.axi_ar_id_o      := AXIRW0.io.axi_ar_id_o    
// io.axi_ar_user_o    := AXIRW0.io.axi_ar_user_o  
// io.axi_ar_len_o     := AXIRW0.io.axi_ar_len_o   
// io.axi_ar_size_o    := AXIRW0.io.axi_ar_size_o  
// io.axi_ar_burst_o   := AXIRW0.io.axi_ar_burst_o 
// io.axi_ar_lock_o    := AXIRW0.io.axi_ar_lock_o  
// io.axi_ar_cache_o   := AXIRW0.io.axi_ar_cache_o 
// io.axi_ar_qos_o     := AXIRW0.io.axi_ar_qos_o   
// io.axi_ar_region_o  := AXIRW0.io.axi_ar_region_o

// io.axi_r_ready_o    := AXIRW0.io.axi_r_ready_o
    // io.skip_diff := MEM2WB0.io.WBLoad_flag && (RegNext(EX2MEM0.io.MEMraddr) === "ha0000048".U(64.W))
}