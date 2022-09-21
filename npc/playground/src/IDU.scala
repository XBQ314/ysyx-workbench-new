import chisel3._
import chisel3.util._

// class Compare extends Module
// {
//     val io = IO(new Bundle
//     {
//         val in1 = Input(UInt(64.W))
//         val in2 = Input(UInt(64.W))
//         val ctrl = Input(UInt(2.W)) // 0-->'<';1-->'>=';2-->'==';3-->'!='
//         val signed_flag = Input(UInt(1.W)) //0-->unsigned;1-->signed
        
//         val out = Output(Bool())
//     })
//     val in2_real = ~io.in2
//     val out64 = Wire(UInt(64.W))
//     val out65 = Wire(UInt(65.W))
//     out65 := io.in1 +& in2_real +& 1.U
//     out64 := out65(63, 0)

//     val ZF = Wire(Bool())
//     val SF = Wire(Bool())
//     val OF = Wire(Bool())
//     val CF = Wire(Bool())
//     ZF := (out64 === 0.U)
//     SF := (out64(63))
//     OF := (io.in1(63) === 0.U && in2_real(63) === 0.U && io.out(63) === 1.U) ||
//           (io.in1(63) === 1.U && in2_real(63) === 1.U && io.out(63) === 0.U)
//     CF := out65(64) ^ 1.U

//     io.out := false.B
//     switch(io.ctrl)
//     {
//     is("b00".U){io.out := Mux(io.signed_flag === 1.U, io.SF ^ io.OF, io.CF)}
//     is("b01".U){io.out := Mux(io.signed_flag === 1.U, !(io.SF ^ io.OF), !(io.CF))}
//     is("b10".U){io.out := io.ZF}
//     is("b11".U){io.out := !(io.ZF)}
//     }
// }

class IDU extends Module
{
    val io = IO(new Bundle
    {
        val pc = Input(UInt(64.W))
        val inst = Input(UInt(32.W))

        val regfile_out1 = Input(UInt(64.W)) // 计算jalr的目标跳转地址需要使用
        val mtvec_out = Input(UInt(64.W)) // needed by ecall
        val mepc_out = Input(UInt(64.W)) // needed by mret

        val rd = Output(UInt(5.W)) //to RegFiles
        val rs1 = Output(UInt(5.W)) //to RegFiles
        val rs2 = Output(UInt(5.W)) //to RegFiles
        val csridx = Output(UInt(8.W))

        val imm = Output(UInt(64.W))
        val zimm = Output(UInt(5.W))
        val shamt = Output(UInt(6.W))

        val ALUctrl = Output(new ALUctrl)
        val IFUctrl = Output(new IFUctrl)
        val LOADctrl = Output(UInt(3.W))
        val Wmask = Output(UInt(8.W))
        
        val pc_next = Output(UInt(64.W))
        val jump_flag = Output(Bool()) // 负责探测jal和jalr造成的跳转
        val flush_req = Output(Bool())
        val mul_flag = Output(Bool())
        val mul_signed = Output(UInt(2.W))
        val mul_outh = Output(UInt(2.W))
        val div_flag = Output(Bool())
        val div_signed = Output(Bool())
        val Btype_flag = Output(Bool())
        val Load_flag = Output(Bool())
        val enw = Output(Bool()) // Reg enw
        val csr_enw = Output(Bool())
        val fencei_flag = Output(Bool())
        // val ecall_flag = Output(Bool())
    })
    val ins = Instructions
    val ImmCalcu1 = Module(new ImmCalcu())

    // val opcode = io.inst(6, 0)
    // val funct3 = io.inst(14, 12)

    io.rd := io.inst(11, 7)
    io.rs1 := io.inst(19, 15)
    io.rs2 := io.inst(24, 20)
    io.csridx := io.inst(27, 20)
    io.imm := ImmCalcu1.io.imm
    io.zimm := io.inst(19, 15)
    io.shamt := io.inst(25, 20)
    ImmCalcu1.io.inst := io.inst
    
    val ctrlList = ListLookup(io.inst,
                    List("b000".U, "b0000_0000_0_0000_00_000_000_00".U(20.W), 0.U, 0.U, 0.U, "b000".U, "h00".U, false.B),
                    // (立即数种类，ALU控制信号，IFU控制信号，LOAD控制信号，写掩码，寄存器组写使能)
    Array
    (
        ins.ebreak->List("b000".U, "b0000_0000_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, false.B),
        ins.ecall ->List("b000".U, "b0000_0000_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, false.B),

        //R-type
        ins.add ->  List("b000".U, "b0000_0000_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.addw->  List("b000".U, "b0000_0000_0_0000_00_001_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.sub ->  List("b000".U, "b0000_0000_1_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.subw->  List("b000".U, "b0000_0000_1_0000_00_001_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        
        ins.mul     -> List("b000".U, "b0000_0000_0_0000_00_100_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.mulh    -> List("b000".U, "b0000_0000_0_0000_00_100_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.mulhsu  -> List("b000".U, "b0000_0000_0_0000_00_100_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.mulhu   -> List("b000".U, "b0000_0000_0_0000_00_100_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.mulw    -> List("b000".U, "b0000_0000_0_0000_00_101_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.divw    -> List("b000".U, "b0010_0010_0_0000_01_101_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.divu    -> List("b000".U, "b0000_0000_0_0000_01_100_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.divuw   -> List("b000".U, "b0010_0010_0_0000_01_101_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.remw    -> List("b000".U, "b0010_0010_0_0000_10_101_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.remuw   -> List("b000".U, "b0010_0010_0_0000_10_101_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.remu    -> List("b000".U, "b0000_0000_0_0000_10_100_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),

        ins.sll ->  List("b000".U, "b0000_0000_0_0000_00_010_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.sllw->  List("b000".U, "b0000_0011_0_0000_00_011_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.srlw->  List("b000".U, "b0010_0011_0_0010_00_011_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.sraw->  List("b000".U, "b0010_0011_0_0010_00_011_100_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.and ->  List("b000".U, "b0000_0000_0_0100_00_010_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.or  ->  List("b000".U, "b0000_0000_0_0110_00_010_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.xor ->  List("b000".U, "b0000_0000_0_1000_00_010_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),

        ins.slt ->  List("b000".U, "b0000_0000_1_0000_00_000_100_01".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.sltu -> List("b000".U, "b0000_0000_1_0000_00_000_000_01".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),

        ins.mret->  List("b000".U, "b0000_0000_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, false.B),
        //I-type
        ins.lb ->   List("b000".U, "b0000_0100_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b001".U, "h00".U, true.B),
        ins.lbu ->  List("b000".U, "b0000_0100_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b010".U, "h00".U, true.B),
        ins.lh ->   List("b000".U, "b0000_0100_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b011".U, "h00".U, true.B),
        ins.lhu ->  List("b000".U, "b0000_0100_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b100".U, "h00".U, true.B),
        ins.lw ->   List("b000".U, "b0000_0100_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b101".U, "h00".U, true.B),
        ins.lwu ->  List("b000".U, "b0000_0100_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b110".U, "h00".U, true.B),
        ins.ld ->   List("b000".U, "b0000_0100_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b111".U, "h00".U, true.B),
        
        ins.addi -> List("b000".U, "b0000_0100_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.addiw-> List("b000".U, "b0000_0100_0_0000_00_001_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),

        ins.slli -> List("b000".U, "b0000_1100_0_0000_00_010_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.slliw-> List("b000".U, "b0000_1100_0_0000_00_011_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.srai -> List("b000".U, "b0000_1100_0_0011_00_010_100_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.sraiw-> List("b000".U, "b0010_1100_0_0010_00_011_100_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.srli -> List("b000".U, "b0000_1100_0_0010_00_010_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.srliw-> List("b000".U, "b0010_1100_0_0010_00_011_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.andi -> List("b000".U, "b0000_0100_0_0100_00_010_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.ori ->  List("b000".U, "b0000_0100_0_0110_00_010_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.xori -> List("b000".U, "b0000_0100_0_1000_00_010_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),

        ins.sltiu-> List("b000".U, "b0000_0100_1_0000_00_000_000_01".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.jalr -> List("b000".U, "b0100_1000_0_0000_00_000_000_00".U, 1.U, 1.U, 1.U, "b000".U, "h00".U, true.B),
        
        ins.csrrs-> List("b000".U, "b0000_0000_0_0110_00_010_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.csrrw-> List("b000".U, "b0000_0000_0_0000_00_010_000_11".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.csrrsi->List("b000".U, "b1000_0000_0_0110_00_010_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.csrrci->List("b000".U, "b1100_0000_0_0100_00_010_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        //S-type
        ins.sb ->   List("b010".U, "b0000_0100_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h01".U, false.B),
        ins.sh ->   List("b010".U, "b0000_0100_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h03".U, false.B),
        ins.sw ->   List("b010".U, "b0000_0100_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h0f".U, false.B),
        ins.sd ->   List("b010".U, "b0000_0100_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b000".U, "hff".U, false.B),
        //B-type
        ins.beq ->  List("b011".U, "b0000_0000_1_0000_00_000_010_01".U, 2.U, 0.U, 0.U, "b000".U, "h00".U, false.B),
        ins.bne ->  List("b011".U, "b0000_0000_1_0000_00_000_011_01".U, 2.U, 0.U, 0.U, "b000".U, "h00".U, false.B),
        ins.blt ->  List("b011".U, "b0000_0000_1_0000_00_000_100_01".U, 2.U, 0.U, 0.U, "b000".U, "h00".U, false.B),
        ins.bge ->  List("b011".U, "b0000_0000_1_0000_00_000_101_01".U, 2.U, 0.U, 0.U, "b000".U, "h00".U, false.B),
        ins.bltu->  List("b011".U, "b0000_0000_1_0000_00_000_000_01".U, 2.U, 0.U, 0.U, "b000".U, "h00".U, false.B),
        ins.bgeu->  List("b011".U, "b0000_0000_1_0000_00_000_001_01".U, 2.U, 0.U, 0.U, "b000".U, "h00".U, false.B),
        //U-type
        ins.auipc-> List("b001".U, "b0100_0100_0_0000_00_000_000_00".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        ins.lui ->  List("b001".U, "b0000_0100_0_0000_00_000_000_10".U, 0.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
        //J-type
        ins.jal ->  List("b100".U, "b0100_1000_0_0000_00_000_000_00".U, 1.U, 0.U, 0.U, "b000".U, "h00".U, true.B),
    ))
    ImmCalcu1.io.imm_sel:=ctrlList(0)
    io.ALUctrl := ctrlList(1).asTypeOf(io.ALUctrl)
    // io.ALUctrl.Calcuin1_ctrl := ctrlList(1)(14, 11)
    // io.ALUctrl.Calcuin2_ctrl := ctrlList(1)(10, 7)
    // io.ALUctrl.sub_flag :=      ctrlList(1)(6)
    // io.ALUctrl.Calcuout_ctrl := ctrlList(1)(5)
    // io.ALUctrl.signed_flag :=   ctrlList(1)(4)  
    // io.ALUctrl.compare_ctrl :=  ctrlList(1)(3, 2)
    // io.ALUctrl.ALUout_ctrl :=   ctrlList(1)(1, 0)
    io.IFUctrl.ifuMux1:=ctrlList(2)
    io.IFUctrl.ifuMux2:=ctrlList(3)
    io.IFUctrl.ifuOutMux:=ctrlList(4)

    io.LOADctrl := ctrlList(5)
    io.Wmask := ctrlList(6)
    io.enw := (ctrlList(7) === true.B) && (io.inst(11, 7) =/= 0.U)

    io.div_flag := ((ctrlList(1)(9, 8) === "b01".U) && (ctrlList(1)(7, 6) === "b10".U)) ||
                   ((ctrlList(1)(9, 8) === "b10".U) && (ctrlList(1)(7, 6) === "b10".U))
    io.div_signed := (io.inst === ins.remw) || 
                     (io.inst === ins.divw)

    io.mul_signed := 0.U
    io.mul_outh := 0.U
    when(io.inst === ins.mulh)
    {
        io.mul_signed := "b11".U
        io.mul_outh := 1.U
    }.elsewhen(io.inst === ins.mulhsu)
    {
        io.mul_signed := "b10".U
        io.mul_outh := 1.U
    }.elsewhen(io.inst === ins.mulhu)
    {
        io.mul_signed := "b00".U
        io.mul_outh := 1.U
    }
    io.mul_flag := (ctrlList(1)(9, 8) === "b00".U) && (ctrlList(1)(7, 6) === "b10".U)

    io.Btype_flag := Mux(ctrlList(0) === "b011".U, true.B, false.B)
    io.Load_flag := Mux(ctrlList(5) === "b000".U, false.B, true.B)
    io.csr_enw := (io.inst === ins.csrrs) || (io.inst === ins.csrrw) ||(io.inst === ins.csrrsi) || (io.inst === ins.csrrci)
    io.fencei_flag := (io.inst === ins.fence_i)
    // io.ecall_flag := (io.inst === ins.ecall)
    // val IDUCampare0 = Module(new Compare())
    // val compare_result = Wire(Bool())
    // IDUCampare0.io.in1 := io.regfile_out1
    // IDUCampare0.io.in2 := io.regfile_out2
    // IDUCampare0.io.ctrl := ctrlList(1)(3, 2)
    // IDUCampare0.io.signed_flag := ctrlList(1)(4)
    // compare_result := IDUCampare0.io.out

    val pc_next_tmp = Wire(UInt(64.W))
    pc_next_tmp := 0.U
    io.jump_flag := false.B
    io.flush_req := false.B
    io.pc_next := 0.U
    when(io.inst === ins.jal || io.inst === ins.jalr)
    {
        pc_next_tmp  := ImmCalcu1.io.imm + Mux(io.inst === ins.jal, io.pc, io.regfile_out1)
        io.jump_flag := true.B
        io.flush_req := true.B
        io.pc_next := Mux(io.inst === ins.jalr, pc_next_tmp&("hFF_FF_FF_FF_FF_FF_FF_FE".U), pc_next_tmp)
    }
    // .elsewhen(io.inst === ins.ecall)
    // {
    //     pc_next_tmp := io.mtvec_out
    //     io.jump_flag := true.B
    //     io.flush_req := true.B
    //     io.pc_next := pc_next_tmp
    // }.elsewhen(io.inst === ins.mret)
    // {
    //     pc_next_tmp := io.mepc_out
    //     io.jump_flag := true.B
    //     io.flush_req := true.B
    //     io.pc_next := pc_next_tmp  
    // }
}