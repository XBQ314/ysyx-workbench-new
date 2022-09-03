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

        // // Advanced eXtensible Interface
        // val axi_aw_ready_i = Input(UInt(1.W))
        // val axi_aw_valid_o = Output(UInt(1.W))
        // val axi_aw_addr_o = Output(UInt(32.W))
        // val axi_aw_prot_o = Output(UInt(3.W))
        // val axi_aw_id_o = Output(UInt(4.W))
        // val axi_aw_user_o = Output(UInt(1.W))
        // val axi_aw_len_o = Output(UInt(8.W))
        // val axi_aw_size_o = Output(UInt(3.W))
        // val axi_aw_burst_o = Output(UInt(2.W))
        // val axi_aw_lock_o = Output(UInt(1.W))
        // val axi_aw_cache_o = Output(UInt(4.W))
        // val axi_aw_qos_o = Output(UInt(4.W))
        // val axi_aw_region_o = Output(UInt(4.W))


        // val axi_w_ready_i = Input(UInt(1.W))
        // val axi_w_valid_o = Output(UInt(1.W))
        // val axi_w_data_o = Output(UInt(64.W))
        // val axi_w_strb_o = Output(UInt(8.W))
        // val axi_w_last_o = Output(UInt(1.W))
        // val axi_w_user_o = Output(UInt(1.W))


        // val axi_b_ready_o = Output(UInt(1.W)) 
        // val axi_b_valid_i = Input(UInt(1.W))
        // val axi_b_resp_i = Input(UInt(2.W))
        // val axi_b_id_i = Input(UInt(4.W))
        // val axi_b_user_i = Input(UInt(1.W))


        // val axi_ar_ready_i = Input(UInt(1.W))
        // val axi_ar_valid_o = Output(UInt(1.W))
        // val axi_ar_addr_o = Output(UInt(32.W))
        // val axi_ar_prot_o = Output(UInt(3.W))
        // val axi_ar_id_o = Output(UInt(4.W))
        // val axi_ar_user_o = Output(UInt(1.W))
        // val axi_ar_len_o = Output(UInt(8.W))
        // val axi_ar_size_o = Output(UInt(3.W))
        // val axi_ar_burst_o = Output(UInt(2.W))
        // val axi_ar_lock_o = Output(UInt(1.W))
        // val axi_ar_cache_o = Output(UInt(4.W))
        // val axi_ar_qos_o = Output(UInt(4.W))
        // val axi_ar_region_o = Output(UInt(4.W))


        // val axi_r_ready_o = Output(UInt(1.W))
        // val axi_r_valid_i = Input(UInt(1.W))
        // val axi_r_resp_i = Input(UInt(2.W))
        // val axi_r_data_i = Input(UInt(64.W))
        // val axi_r_last_i = Input(UInt(1.W))
        // val axi_r_id_i = Input(UInt(4.W))
        // val axi_r_user_i = Input(UInt(1.W))
        
        // SOC io_interrupt
        val interrupt = Input(UInt(1.W))

        // SOC Interface Master
        val master_awready =    Input(UInt(1.W))
        val master_awvalid =    Output(UInt(1.W))
        val master_awaddr =     Output(UInt(32.W)) 
        val master_awid =       Output(UInt(4.W))
        val master_awlen =      Output(UInt(8.W))
        val master_awsize =     Output(UInt(3.W))
        val master_awburst =    Output(UInt(2.W))

        val master_wready =     Input(UInt(1.W))
        val master_wvalid =     Output(UInt(1.W))
        val master_wdata =      Output(UInt(64.W))
        val master_wstrb =      Output(UInt(8.W))
        val master_wlast =      Output(UInt(1.W))

        val master_bready =     Output(UInt(1.W)) 
        val master_bvalid =     Input(UInt(1.W))
        val master_bresp =      Input(UInt(2.W))
        val master_bid =        Input(UInt(4.W))

        val master_arready =    Input(UInt(1.W))
        val master_arvalid =    Output(UInt(1.W))
        val master_araddr =     Output(UInt(32.W))
        val master_arid =       Output(UInt(4.W))   
        val master_arlen =      Output(UInt(8.W))
        val master_arsize =     Output(UInt(3.W))
        val master_arburst =    Output(UInt(2.W))

        val master_rready =     Output(UInt(1.W)) 
        val master_rvalid =     Input(UInt(1.W))
        val master_rresp =      Input(UInt(2.W))
        val master_rdata =      Input(UInt(64.W))
        val master_rlast =      Input(UInt(1.W))
        val master_rid =        Input(UInt(4.W))

        // SOC Interface Slave   
        val slave_awready    = Output(UInt(1.W))
        val slave_awvalid    = Input(UInt(1.W))
        val slave_awaddr     = Input(UInt(32.W)) 
        val slave_awid       = Input(UInt(4.W))   
        val slave_awlen      = Input(UInt(8.W))  
        val slave_awsize     = Input(UInt(3.W)) 
        val slave_awburst    = Input(UInt(2.W))

        val slave_wready     = Output(UInt(1.W))
        val slave_wvalid     = Input(UInt(1.W))
        val slave_wdata      = Input(UInt(64.W))  
        val slave_wstrb      = Input(UInt(8.W)) 
        val slave_wlast      = Input(UInt(1.W))  

        val slave_bready     = Input(UInt(1.W)) 
        val slave_bvalid     = Output(UInt(1.W))
        val slave_bresp      = Output(UInt(2.W)) 
        val slave_bid        = Output(UInt(4.W))  

        val slave_arready    = Output(UInt(1.W))
        val slave_arvalid    = Input(UInt(1.W))
        val slave_araddr     = Input(UInt(32.W))
        val slave_arid       = Input(UInt(4.W))     
        val slave_arlen      = Input(UInt(8.W))  
        val slave_arsize     = Input(UInt(3.W)) 
        val slave_arburst    = Input(UInt(2.W))

        val slave_rready     = Input(UInt(1.W))  
        val slave_rvalid     = Output(UInt(1.W)) 
        val slave_rresp      = Output(UInt(2.W))  
        val slave_rdata      = Output(UInt(64.W))  
        val slave_rlast      = Output(UInt(1.W))  
        val slave_rid        = Output(UInt(4.W))
    })
    val IFU0 = Module(new IFU())
    val IFU_DPI0 = Module(new ysyx_22040154_IFU_DPI()) // verilog
    val IF2ID0 = Module(new IF2ID())
    val IDU0 = Module(new IDU())
    val ID2EX0 = Module(new ID2EX())
    val ALU0 = Module(new ALU())
    val RegisterFiles0 = Module(new ysyx_22040154_RegisterFiles()) // verilog
    val EX2MEM0 = Module(new EX2MEM())
    // val MEM0 = Module(new MEM())
    val MEMCTRL0 = Module(new MEMCTRL())
    val MEM_DPI0 = Module(new ysyx_22040154_MEM_DPI()) // verilog
    val LOADUNIT0 = Module(new ysyx_22040154_LOADUNIT()) // verilog
    val MEM2WB0 = Module(new MEM2WB())
    val CTRL0 = Module(new CTRL())
    val CSR0 = Module(new ysyx_22040154_CSR()) // verilog
    val CLINT0 = Module(new CLINT())
    val ICACHE_CTRL0 = Module(new CACHE_CTRL())
    val ICACHE0 = Module(new ICACHE())
    val DCACHE_CTRL0 = Module(new DCACHE_CTRL())
    val DCACHE0 = Module(new DCACHE())
    val AXIRW0 = Module(new ysyx_22040154_axi_rw()) // verilog
    val AXI_ARIBITER0 = Module(new AXI_ARIBITER())

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

    AXIRW0.io.clock         := clock // AXI
    AXIRW0.io.reset         := reset // AXI
    // AXIRW0.io.rw_addr_i     := ICACHE_CTRL0.io.addr2mem(31, 0) // AXI
    // AXIRW0.io.rw_valid_i    := ICACHE_CTRL0.io.valid2mem // AXI
    // AXIRW0.io.enw_i         := ICACHE_CTRL0.io.enw2mem // AXI
    // AXIRW0.io.rw_w_data_i   := ICACHE_CTRL0.io.data2mem // AXI
    // AXIRW0.io.rw_size_i     := ICACHE_CTRL0.io.wmask2mem // AXI
    AXIRW0.io.rw_addr_i     := AXI_ARIBITER0.io.rw_addr_i // AXI
    AXIRW0.io.rw_valid_i    := AXI_ARIBITER0.io.rw_valid_i // AXI
    AXIRW0.io.enw_i         := AXI_ARIBITER0.io.enw_i // AXI
    AXIRW0.io.rw_w_data_i   := AXI_ARIBITER0.io.rw_w_data_i // AXI
    AXIRW0.io.rw_size_i     := AXI_ARIBITER0.io.rw_size_i // AXI

    AXIRW0.io.axi_aw_ready_i:= io.master_awready // AXI
    AXIRW0.io.axi_w_ready_i := io.master_wready // AXI
    AXIRW0.io.axi_b_valid_i := io.master_bvalid // AXI 
    AXIRW0.io.axi_b_resp_i  := io.master_bresp // AXI
    AXIRW0.io.axi_b_id_i    := io.master_bid // AXI
    AXIRW0.io.axi_b_user_i  := 0.U // AXI
    AXIRW0.io.axi_ar_ready_i:= io.master_arready // AXI
    AXIRW0.io.axi_r_valid_i := io.master_rvalid // AXI
    AXIRW0.io.axi_r_resp_i  := io.master_rresp // AXI
    AXIRW0.io.axi_r_data_i  := io.master_rdata // AXI
    AXIRW0.io.axi_r_last_i  := io.master_rlast // AXI
    AXIRW0.io.axi_r_id_i    := io.master_rid // AXI
    AXIRW0.io.axi_r_user_i  := 0.U // AXI

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
    //ID and EX
    ID2EX0.io.enID2EX   := !CTRL0.io.stall_id2ex
    ID2EX0.io.flush     := CTRL0.io.flush_id2ex

    ID2EX0.io.IDimm         := IDU0.io.imm
    ID2EX0.io.IDzimm        := IDU0.io.zimm
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
    ID2EX0.io.IDcsr_rd          := IDU0.io.csridx
    ID2EX0.io.IDcsrout          := CSR0.io.csr_out
    ID2EX0.io.IDclint_enw       := CLINT0.io.csr_enw
    ID2EX0.io.IDclint_mstatus   := CLINT0.io.mstatus_out
    ID2EX0.io.IDclint_mepc      := CLINT0.io.mepc_out
    ID2EX0.io.IDclint_mcause    := CLINT0.io.mcause_out
    ID2EX0.io.IDdiv_flag        := IDU0.io.div_flag
    ID2EX0.io.IDdiv_signed      := IDU0.io.div_signed
    ID2EX0.io.IDmul_flag        := IDU0.io.mul_flag
    ID2EX0.io.IDBtype_flag      := IDU0.io.Btype_flag
    ID2EX0.io.IDLoad_flag       := IDU0.io.Load_flag
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
    ALU0.io.Btype_flag   := ID2EX0.io.EXBtype_flag

    RegisterFiles0.io.clock     := clock
    RegisterFiles0.io.reset     := reset
    RegisterFiles0.io.read1_idx := IDU0.io.rs1
    RegisterFiles0.io.read2_idx := IDU0.io.rs2

    CSR0.io.clock     := clock
    CSR0.io.reset     := reset
    CSR0.io.read_idx  := IDU0.io.csridx

    CLINT0.io.inst          := IF2ID0.io.IDinst
    CLINT0.io.pc            := IF2ID0.io.IDpc
    CLINT0.io.global_int_en := CSR0.io.global_int_en
    CLINT0.io.int_flag      := io.interrupt
    CLINT0.io.mstatus_in    := CSR0.io.mstatus_out
    CLINT0.io.mepc_in       := CSR0.io.mepc_out
    CLINT0.io.mtvec_in      := CSR0.io.mtvec_out

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
    EX2MEM0.io.EXLoad_flag      := ID2EX0.io.EXLoad_flag
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
    DCACHE_CTRL0.io.uncached_flag := MEMCTRL0.io.uncached_flag

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

    MEM_DPI0.io.clock := clock
    // MEM_DPI0.io.raddr := Mux(DCACHE_CTRL0.io.uart_dpi_flag, DCACHE_CTRL0.io.addr2mem, 0.U) // DPI-C
    // MEM_DPI0.io.waddr := Mux(DCACHE_CTRL0.io.uart_dpi_flag, DCACHE_CTRL0.io.addr2mem, 0.U) // DPI-C
    MEM_DPI0.io.raddr := DCACHE_CTRL0.io.addr2mem // DPI-C
    MEM_DPI0.io.waddr := DCACHE_CTRL0.io.addr2mem // DPI-C
    MEM_DPI0.io.wdata := DCACHE_CTRL0.io.data2mem // DPI-C
    MEM_DPI0.io.wmask := DCACHE_CTRL0.io.wmask2mem // DPI-C

    LOADUNIT0.io.raddr := EX2MEM0.io.MEMraddr
    LOADUNIT0.io.rdata_native := DCACHE_CTRL0.io.data2cpu
    LOADUNIT0.io.LOADctrl := EX2MEM0.io.MEMLOADctrl


    //MEM and WB
    MEM2WB0.io.enMEM2WB    := !CTRL0.io.stall_mem2wb
    MEM2WB0.io.flush       := CTRL0.io.flush_mem2wb
 
    MEM2WB0.io.MEMrd       := EX2MEM0.io.MEMrd
    MEM2WB0.io.MEMenw      := EX2MEM0.io.MEMenw
    MEM2WB0.io.MEMcsr_enw  := EX2MEM0.io.MEMcsr_enw
    // MEM2WB0.io.MEMwrb2reg  := Mux(MEM2WB0.io.MEMLoad_flag, MEM0.io.rdata, EX2MEM0.io.MEMwrb2reg)
    MEM2WB0.io.MEMwrb2reg  := Mux(MEM2WB0.io.MEMLoad_flag, LOADUNIT0.io.rdata, EX2MEM0.io.MEMwrb2reg)
    
    MEM2WB0.io.MEMcsr_rd   := EX2MEM0.io.MEMcsr_rd
    MEM2WB0.io.MEMwrb2csr  := EX2MEM0.io.MEMwrb2csr
    // MEM2WB0.io.MEMmemout   := MEM0.io.rdata
    MEM2WB0.io.MEMmemout   := LOADUNIT0.io.rdata // !!!

    MEM2WB0.io.MEMLOADctrl := EX2MEM0.io.MEMLOADctrl
    MEM2WB0.io.MEMwaddr    := EX2MEM0.io.MEMwaddr
    MEM2WB0.io.MEMclint_enw:= EX2MEM0.io.MEMclint_enw
    MEM2WB0.io.MEMclint_mstatus:= EX2MEM0.io.MEMclint_mstatus
    MEM2WB0.io.MEMclint_mepc   :=  EX2MEM0.io.MEMclint_mepc  
    MEM2WB0.io.MEMclint_mcause :=  EX2MEM0.io.MEMclint_mcause
    MEM2WB0.io.MEMLoad_flag:= EX2MEM0.io.MEMLoad_flag
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
    
    //CTRL
    CTRL0.io.id_rs1         := IDU0.io.rs1
    CTRL0.io.id_rs2         := IDU0.io.rs2
    CTRL0.io.ex_rd          := ID2EX0.io.EXrd
    CTRL0.io.ex_enw         := ID2EX0.io.EXenw
    CTRL0.io.mem_rd         := EX2MEM0.io.MEMrd
    CTRL0.io.mem_enw        := EX2MEM0.io.MEMenw
    CTRL0.io.wb_rd          := MEM2WB0.io.WBrd
    CTRL0.io.wb_enw         := MEM2WB0.io.WBenw
    CTRL0.io.flushreq_id    := IDU0.io.flush_req || CLINT0.io.int_jump_flag
    CTRL0.io.flushreq_ex    := ALU0.io.flush_req
    CTRL0.io.ifu_stall_req  := IFU0.io.ifu_stall_req
    CTRL0.io.dcache_stall_req  := MEMCTRL0.io.memstall_req
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

    AXI_ARIBITER0.io.rw_ready_o         := AXIRW0.io.rw_ready_o
    AXI_ARIBITER0.io.data_read_o        := AXIRW0.io.data_read_o

    //top input
    io.pc           := MEM2WB0.io.WBpc
    io.inst         := MEM2WB0.io.WBinst
    io.mem_pc       := EX2MEM0.io.MEMpc
    io.mem_addr     := EX2MEM0.io.MEMwaddr
    io.mem_wdata    := EX2MEM0.io.MEMwdata
    io.enMEM2WB     := MEM2WB0.io.enMEM2WB

    // SOC AXI-Master
    io.master_awvalid  := AXIRW0.io.axi_aw_valid_o 
    io.master_awaddr   := AXIRW0.io.axi_aw_addr_o
    io.master_awid     := AXIRW0.io.axi_aw_id_o
    io.master_awlen    := AXIRW0.io.axi_aw_len_o
    io.master_awsize   := AXIRW0.io.axi_aw_size_o
    io.master_awburst  := AXIRW0.io.axi_aw_burst_o

    io.master_wvalid   := AXIRW0.io.axi_w_valid_o
    io.master_wdata    := AXIRW0.io.axi_w_data_o
    io.master_wstrb    := AXIRW0.io.axi_w_strb_o
    io.master_wlast    := AXIRW0.io.axi_w_last_o

    io.master_bready   := AXIRW0.io.axi_b_ready_o

    io.master_arvalid  := AXIRW0.io.axi_ar_valid_o
    io.master_araddr   := AXIRW0.io.axi_ar_addr_o
    io.master_arid     := AXIRW0.io.axi_ar_id_o
    io.master_arlen    := AXIRW0.io.axi_ar_len_o
    io.master_arsize   := AXIRW0.io.axi_ar_size_o
    io.master_arburst  := AXIRW0.io.axi_ar_burst_o

    io.master_rready   := AXIRW0.io.axi_r_ready_o

    // SOC AXI-Slave
    io.slave_awready    := 0.U

    io.slave_wready     := 0.U

    io.slave_bvalid     := 0.U
    io.slave_bresp      := 0.U
    io.slave_bid        := 0.U

    io.slave_arready    := 0.U

    io.slave_rvalid     := 0.U
    io.slave_rresp      := 0.U
    io.slave_rdata      := 0.U
    io.slave_rlast      := 0.U
    io.slave_rid        := 0.U
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