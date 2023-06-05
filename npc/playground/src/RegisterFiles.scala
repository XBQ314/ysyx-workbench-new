import chisel3._
import chisel3.util._

// class RegisterFiles extends Module
// {
//     val io = IO(new Bundle
//     {
//         val read1_idx = Input(UInt(5.W)) //from IDU
//         val read2_idx = Input(UInt(5.W)) //from IDU
//         val write_idx = Input(UInt(5.W)) //from IDU
//         val enw = Input(Bool())
//         val in_data = Input(UInt(64.W)) //from ALU
        
//         val regfile_out1 = Output(UInt(64.W))
//         val regfile_out2 = Output(UInt(64.W))
//     })
//     val regFiles = RegInit(VecInit(Seq.fill(32)(0.U(64.W))))
//     // val regFiles = RegInit(VecInit.tabulate(32){0.U(64.W)})
//     // val regFiles = Reg(Vec(32, UInt(64.W)))
    
//     for(i <- 0 until 32)
//     {
//         if(i == 0)
//         {
//             regFiles(i) := 0.U
//         }
//         else
//         {
//             regFiles(i) := Mux((io.enw === 1.B) && (io.write_idx === i.U), io.in_data, regFiles(i))
//         }
//     }
//     io.regfile_out1 := regFiles(io.read1_idx)
//     io.regfile_out2 := regFiles(io.read2_idx)
// }

class ysyx_040154_RegisterFiles extends BlackBox with HasBlackBoxInline
{
    val io = IO(new Bundle
    {
        val clock = Input(Clock())
        val reset  = Input(Bool())
        val read1_idx = Input(UInt(5.W)) //from IDU
        val read2_idx = Input(UInt(5.W)) //from IDU
        val write_idx = Input(UInt(5.W)) //from IDU
        val enw = Input(Bool())
        val in_data = Input(UInt(64.W)) //from ALU
        
        val regfile_out1 = Output(UInt(64.W))
        val regfile_out2 = Output(UInt(64.W))
    })
    setInline("RegisterFiles.v",
                """
|module ysyx_040154_RegisterFiles
|(
|    input clock,
|    input reset,
|    input enw,
|    input [4:0]read1_idx,
|    input [4:0]read2_idx,
|    input [4:0]write_idx,
|    input [63:0]in_data,
|
|    output [63:0]regfile_out1,
|    output [63:0]regfile_out2
|);
|reg [63:0] regFiles[31:0];
|
|//import "DPI-C" function void set_gpr_ptr(input logic [63:0] a []);
|//initial 
|//begin
|//    set_gpr_ptr(regFiles); 
|//end
|
|always@(posedge clock)
|begin
|    if(reset)
|    begin
|        regFiles[0] <= 'd0;regFiles[4] <= 'd0;regFiles[8] <= 'd0;regFiles[12] <= 'd0;
|        regFiles[1] <= 'd0;regFiles[5] <= 'd0;regFiles[9] <= 'd0;regFiles[13] <= 'd0;
|        regFiles[2] <= 'd0;regFiles[6] <= 'd0;regFiles[10] <= 'd0;regFiles[14] <= 'd0;
|        regFiles[3] <= 'd0;regFiles[7] <= 'd0;regFiles[11] <= 'd0;regFiles[15] <= 'd0;
|        
|        regFiles[16] <= 'd0;regFiles[20] <= 'd0;regFiles[24] <= 'd0;regFiles[28] <= 'd0;
|        regFiles[17] <= 'd0;regFiles[21] <= 'd0;regFiles[25] <= 'd0;regFiles[29] <= 'd0;
|        regFiles[18] <= 'd0;regFiles[22] <= 'd0;regFiles[26] <= 'd0;regFiles[30] <= 'd0;
|        regFiles[19] <= 'd0;regFiles[23] <= 'd0;regFiles[27] <= 'd0;regFiles[31] <= 'd0;
|    end
|    else
|    begin
|        if(enw) 
|        begin
|            regFiles[write_idx] <= (|write_idx?in_data:'d0);
|        end
|    end
|end
|
|assign regfile_out1 = regFiles[read1_idx];
|assign regfile_out2 = regFiles[read2_idx];
|
|// always@(*)
|// begin
|//     if((enw == 1'b1)&&
|//         (read1_idx == write_idx))
|//     begin
|//         regfile_out1_r = in_data;
|//     end
|//     else
|//     begin
|//         regfile_out1_r = regFiles[read1_idx];
|//     end
|// end
|// 
|// always@(*)
|// begin
|//     if((enw == 1'b1)&&
|//         (read2_idx == write_idx))
|//     begin
|//         regfile_out2_r = in_data;
|//     end
|//     else
|//     begin
|//         regfile_out2_r = regFiles[read2_idx];
|//     end
|// end
|
|endmodule
|
                """.stripMargin)
}