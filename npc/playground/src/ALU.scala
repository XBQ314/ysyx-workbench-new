import chisel3._
import chisel3.util._

class ALUctrl extends Bundle
{
    val Calcuin1_ctrl = Input(UInt(4.W)) //from IDU
    val Calcuin2_ctrl = Input(UInt(4.W)) //from IDU

    val sub_flag = Input(UInt(1.W)) //from IDU

    val Bit_ctrl = Input(UInt(3.W)) //from IDU
    val Bit_signedlen = Input(UInt(1.W))

    val Muldiv_ctrl =Input(UInt(2.W))
    val Calcuout_ctrl = Input(UInt(3.W)) //from IDU
    val signed_flag = Input(UInt(1.W)) //from IDU
    val compare_ctrl = Input(UInt(2.W)) //from IDU

    val ALUout_ctrl = Input(UInt(2.W)) //from IDU
}

class ADD extends Module
{
    val io = IO(new Bundle
    {
        val in1 = Input(UInt(64.W))
        val in2 = Input(UInt(64.W))
        val sub = Input(UInt(1.W))

        val ZF = Output(UInt(1.W))
        val SF = Output(UInt(1.W))
        val OF = Output(UInt(1.W))
        val CF = Output(UInt(1.W))
        val out = Output(UInt(64.W))
    })
    val in2_real = Mux(io.sub === 1.U, ~io.in2, io.in2)
    val out65 = Wire(UInt(65.W))
    out65 := io.in1 +& in2_real +& io.sub

    io.out := out65(63,0)
    io.ZF := (io.out === 0.U)
    io.SF := (io.out(63))
    io.OF := (io.in1(63) === 0.U && in2_real(63) === 0.U && io.out(63) === 1.U) ||
             (io.in1(63) === 1.U && in2_real(63) === 1.U && io.out(63) === 0.U)
    io.CF := out65(64) ^ io.sub
}

class BIToperate extends Module
{
    val io = IO(new Bundle
    {
        val in1 = Input(UInt(64.W))
        val in2 = Input(UInt(64.W))
        val ctrl = Input(UInt(3.W)) //000-->'shift left';001-->'shift right'
                                    //010-->'bitwise and';011-->'bitwise or'
                                    //100-->'bitwise xor';
        val signed_flag = Input(UInt(1.W)) //0-->unsigned;1-->signed
        val signed_length = Input(UInt(1.W)) //0-->32bits;1-->64bits

        val out = Output(UInt(64.W))
    })
    val signed_in1_32 = Wire(SInt(32.W))
    val signed_in1_64 = Wire(SInt(64.W))
    signed_in1_32 := io.in1.asTypeOf(chiselTypeOf(signed_in1_32))
    signed_in1_64 := io.in1.asTypeOf(chiselTypeOf(signed_in1_64))

    io.out := 0.U
    switch(io.ctrl)
    {
    is("b000".U){io.out := io.in1 << io.in2(5, 0)}
    is("b001".U)
    {
        when(io.signed_flag === 0.U)
        {
            io.out := io.in1 >> io.in2(5, 0)
        }.otherwise
        {
            io.out := Mux(io.signed_length === 0.U, Cat(Fill(32, signed_in1_32(31)), signed_in1_32 >> io.in2(5, 0)).asTypeOf(io.out),
                                                    (signed_in1_64 >> io.in2(5, 0)).asTypeOf(io.out))
        }
        // when(io.signed_length === 0.U) //32bits
        // {
        //     io.out := Mux(io.signed_flag === 1.U, Cat(Fill(32, signed_in1_32(31)), signed_in1_32 >> io.in2(5, 0)).asTypeOf(io.out), 
        //                                           io.in1 >> io.in2(5, 0))
        // }.otherwise  //64bits
        // {
        //     io.out := Mux(io.signed_flag === 1.U, (signed_in1_64 >> io.in2(5, 0)).asTypeOf(io.out), 
        //                                           io.in1 >> io.in2(5, 0))
        // }
    }
    is("b010".U){io.out := io.in1 & io.in2}
    is("b011".U){io.out := io.in1 | io.in2}
    is("b100".U){io.out := io.in1 ^ io.in2}
    }

}

class Muldiv extends Module
{
    val io = IO(new Bundle
    {
        val clock = Input(Clock())
        val reset = Input(Bool())

        val in1 = Input(UInt(64.W))
        val in2 = Input(UInt(64.W))
        val ctrl = Input(UInt(2.W))

        val mul_flag = Input(Bool())
        val mul_signed = Input(UInt(2.W))
        val mul_outh = Input(UInt(1.W))
        val div_flag = Input(Bool())
        val div_signed = Input(Bool())
        val out_valid = Output(Bool())
        val out = Output(UInt(64.W))
    })
    val MUL0 = Module(new ysyx_040154_MUL())
    val DIV0 = Module(new ysyx_040154_DIV())
    val nxt_state = Wire(UInt(3.W))
    val cur_state = RegNext(nxt_state, "b000".U)
    val mul_valid = Wire(Bool())
    val div_valid = Wire(Bool())
    
    val IDLE = "b000".U
    val INPUT_MUL = "b001".U
    val INPUT_DIV = "b101".U
    val BUSY_MUL = "b010".U
    val BUSY_DIV = "b011".U
    val BUSY_MOD = "b100".U

    nxt_state := IDLE
    mul_valid := false.B
    div_valid := false.B

    //MUL signal
    MUL0.io.clock := io.clock
    MUL0.io.reset := io.reset

    MUL0.io.mul_valid := mul_valid
    MUL0.io.flush := false.B
    // MUL0.io.mulw := false.B
    MUL0.io.mul_signed := io.mul_signed

    MUL0.io.multiplicand := io.in1
    MUL0.io.multipiler := io.in2
    // DIV signal
    DIV0.io.clock := io.clock
    DIV0.io.reset := io.reset

    DIV0.io.div_valid := div_valid
    DIV0.io.flush := false.B
    // DIV0.io.divw := false.B
    DIV0.io.div_signed := io.div_signed

    DIV0.io.dividend := io.in1
    DIV0.io.divisor := io.in2
    io.out_valid := MUL0.io.out_valid || DIV0.io.out_valid

    when(cur_state === IDLE)
    {
        mul_valid := false.B
        div_valid := false.B
        when(io.mul_flag && MUL0.io.mul_ready){nxt_state := INPUT_MUL}
        .elsewhen(io.div_flag && DIV0.io.div_ready){nxt_state := INPUT_DIV}
        .otherwise{nxt_state := IDLE}
    }.elsewhen(cur_state === INPUT_MUL)
    {
        mul_valid := true.B
        div_valid := false.B
        when(io.mul_flag){nxt_state := BUSY_MUL}
        .otherwise{nxt_state := IDLE}
    }.elsewhen(cur_state === INPUT_DIV)
    {
        mul_valid := false.B
        div_valid := true.B
        when(io.div_flag){nxt_state := BUSY_DIV}
        .otherwise{nxt_state := IDLE}
    }.elsewhen((cur_state === BUSY_MUL) || (cur_state === BUSY_DIV))
    {
        mul_valid := false.B
        div_valid := false.B
        when(MUL0.io.out_valid || DIV0.io.out_valid){nxt_state := IDLE}
        .otherwise{nxt_state := BUSY_MUL}
    }

    io.out := 0.U
    switch(io.ctrl)
    {
    is("b00".U){io.out := Mux(io.mul_outh === 1.U, MUL0.io.result_hi, MUL0.io.result_lo)}
    is("b01".U){io.out := DIV0.io.quotient}
    is("b10".U){io.out := DIV0.io.remainder}
    }
}

class CompareSignal extends Module
{
    val io = IO(new Bundle
    {
        val ZF = Input(UInt(1.W))
        val SF = Input(UInt(1.W))
        val OF = Input(UInt(1.W))
        val CF = Input(UInt(1.W))
        val ctrl = Input(UInt(2.W)) // 0-->'<';1-->'>=';2-->'==';3-->'!='
        val signed_flag = Input(UInt(1.W)) //0-->unsigned;1-->signed
        
        val out = Output(UInt(64.W))
    })
    io.out := 0.U
    switch(io.ctrl)
    {
    is("b00".U){io.out := Mux(io.signed_flag === 1.U, io.SF ^ io.OF, io.CF)}
    is("b01".U){io.out := Mux(io.signed_flag === 1.U, !(io.SF ^ io.OF), !(io.CF))}
    is("b10".U){io.out := io.ZF}
    is("b11".U){io.out := !(io.ZF)}
    }
}

class CALCUsrc extends Module
{
    val io = IO(new Bundle
    {
        val in0 = Input(UInt(64.W))
        val in1 = Input(UInt(64.W))
        val in2 = Input(UInt(64.W))
        val in3 = Input(UInt(64.W))
        val ctrl = Input(UInt(3.W))
        val trun_type = Input(UInt(1.W)) // trun=0-->[31:0];trun=1-->[4:0]

        val out = Output(UInt(64.W))
    })
    val muxout1 = WireDefault(0.U(64.W))
    switch(io.ctrl(2,1))
    {
    is("b00".U){muxout1 := io.in0}
    is("b01".U){muxout1 := io.in1}
    is("b10".U){muxout1 := io.in2}
    is("b11".U){muxout1 := io.in3}
    }
    io.out := Mux(io.ctrl(0) === 1.U, Mux(io.trun_type === 1.U, muxout1(4,0), muxout1(31,0)), muxout1)
}

class ALU extends Module
{
    val io = IO(new Bundle
    {
        val clock = Input(Clock())
        val reset = Input(Bool())

        val regfile_out1 = Input(UInt(64.W)) //from regFile
        val regfile_out2 = Input(UInt(64.W)) //from regFile
        val csr_out     = Input(UInt(64.W))
        val csr_enw     = Input(UInt(1.W))
        val imm = Input(UInt(64.W)) //from IDU
        val zimm = Input(UInt(5.W))
        val pc = Input(UInt(64.W)) //from IFU
        val shamt = Input(UInt(6.W))

        // val Calcuin1_ctrl = Input(UInt(4.W))
        // val Calcuin2_ctrl = Input(UInt(4.W))
        // val Calcuout_ctrl = Input(UInt(1.W))
        // val ALUout_ctrl = Input(UInt(2.W))
        // val compare_ctrl = Input(UInt(2.W))
        // val sub_flag = Input(UInt(1.W))
        // val signed_flag = Input(UInt(1.W))
        val ALUctrl = Input(new ALUctrl())

        val div_flag = Input(Bool())
        val div_signed=Input(Bool())
        val mul_signed=Input(UInt(2.W))
        val mul_outh = Input(UInt(1.W))
        val mul_flag = Input(Bool())
        val Btype_flag = Input(Bool())
        val pc_next = Output(UInt(64.W))
        val jump_flag = Output(Bool()) // 负责探测B型造成的跳转
        val flush_req = Output(Bool())
        val divstall_req = Output(Bool())
        val mulstall_req = Output(Bool())

        val ALUout_data = Output(UInt(64.W)) //to regFile
    })
    val calcuSrc1 = Module(new CALCUsrc())
    val calcuSrc2 = Module(new CALCUsrc())
    val calcuAdd = Module(new ADD())
    val calcuBit = Module(new BIToperate())
    val calcuMD = Module(new Muldiv())
    val aluCompare = Module(new CompareSignal())

    // calcuSrc1
    calcuSrc1.io.in0 := io.regfile_out1
    calcuSrc1.io.in1 := io.pc
    calcuSrc1.io.in2 := Cat(0.U(59.W), io.zimm)
    calcuSrc1.io.in3 := ~Cat(0.U(59.W), io.zimm)
    calcuSrc1.io.ctrl := io.ALUctrl.Calcuin1_ctrl(3,1)
    calcuSrc1.io.trun_type := io.ALUctrl.Calcuin1_ctrl(0)

    // calcuSrc2
    calcuSrc2.io.in0 := Mux(io.csr_enw === 0.U, io.regfile_out2, io.csr_out)
    calcuSrc2.io.in1 := io.imm
    calcuSrc2.io.in2 := 4.U
    calcuSrc2.io.in3 := io.shamt
    calcuSrc2.io.ctrl := io.ALUctrl.Calcuin2_ctrl(3,1)
    calcuSrc2.io.trun_type := io.ALUctrl.Calcuin2_ctrl(0)

    // calcuAdd
    calcuAdd.io.in1 := calcuSrc1.io.out
    calcuAdd.io.in2 := calcuSrc2.io.out
    calcuAdd.io.sub := io.ALUctrl.sub_flag

    // calcuBit
    calcuBit.io.in1 := calcuSrc1.io.out
    calcuBit.io.in2 := calcuSrc2.io.out
    calcuBit.io.ctrl := io.ALUctrl.Bit_ctrl
    calcuBit.io.signed_flag := io.ALUctrl.signed_flag
    calcuBit.io.signed_length := io.ALUctrl.Bit_signedlen

    // calcuMD
    calcuMD.io.clock := io.clock
    calcuMD.io.reset := io.reset
    calcuMD.io.in1 := calcuSrc1.io.out
    calcuMD.io.in2 := calcuSrc2.io.out
    calcuMD.io.ctrl := io.ALUctrl.Muldiv_ctrl
    calcuMD.io.div_flag := io.div_flag
    calcuMD.io.div_signed:= io.div_signed
    calcuMD.io.mul_flag := io.mul_flag
    calcuMD.io.mul_signed := io.mul_signed
    calcuMD.io.mul_outh := io.mul_outh
    io.mulstall_req := io.mul_flag && (!calcuMD.io.out_valid)
    io.divstall_req := io.div_flag && (!calcuMD.io.out_valid)

    // aluCompare
    aluCompare.io.ZF := calcuAdd.io.ZF
    aluCompare.io.SF := calcuAdd.io.SF
    aluCompare.io.OF := calcuAdd.io.OF
    aluCompare.io.CF := calcuAdd.io.CF
    aluCompare.io.signed_flag := io.ALUctrl.signed_flag
    aluCompare.io.ctrl := io.ALUctrl.compare_ctrl

    // out
    val calcuOut = Wire(UInt(64.W))
    calcuOut := 0.U
    switch(io.ALUctrl.Calcuout_ctrl)
    {
    is("b000".U){calcuOut := calcuAdd.io.out}
    is("b001".U){calcuOut := Cat(Fill(32, calcuAdd.io.out(31)), calcuAdd.io.out(31,0))}
    is("b010".U){calcuOut := calcuBit.io.out}
    is("b011".U){calcuOut := Cat(Fill(32, calcuBit.io.out(31)), calcuBit.io.out(31,0))}
    is("b100".U){calcuOut := calcuMD.io.out}
    is("b101".U){calcuOut := Cat(Fill(32, calcuMD.io.out(31)), calcuMD.io.out(31,0))}
    }

    io.ALUout_data := 0.U
    switch(io.ALUctrl.ALUout_ctrl)
    {
    is("b00".U){io.ALUout_data := calcuOut}
    is("b01".U){io.ALUout_data := aluCompare.io.out}
    is("b10".U){io.ALUout_data := io.imm}
    is("b11".U){io.ALUout_data := io.regfile_out1}
    // is("b11".U){alu_out := }
    }

    // 计算B型跳转指令的跳转地址
    io.jump_flag := io.Btype_flag && aluCompare.io.out(0)
    io.pc_next := Mux(io.jump_flag, io.pc + io.imm, 0.U)
    io.flush_req := io.jump_flag
}