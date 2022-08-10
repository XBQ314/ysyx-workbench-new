import chisel3._
import chisel3.util._

class S011HD1P_X32Y2D128_BW extends BlackBox with HasBlackBoxInline
{
    val io = IO(new Bundle
    {
        val Q = Output(UInt(128.W))

        val CLK = Input(Clock())
        val CEN = Input(UInt(1.W))
        val WEN = Input(UInt(1.W))
        val BWEN = Input(UInt(128.W))
        val A = Input(UInt(6.W))
        val D = Input(UInt(128.W))
    })
    setInline("S011HD1P_X32Y2D128_BW.v",
                """
|module S011HD1P_X32Y2D128_BW(
|    Q, CLK, CEN, WEN, BWEN, A, D
|);
|parameter Bits = 128;
|parameter Word_Depth = 64;
|parameter Add_Width = 6;
|parameter Wen_Width = 128;
|
|output reg [Bits-1:0] Q;
|input                 CLK;
|input                 CEN;
|input                 WEN;
|input [Wen_Width-1:0] BWEN;
|input [Add_Width-1:0] A;
|input [Bits-1:0]      D;
|
|wire cen  = ~CEN;
|wire wen  = ~WEN;
|wire [Wen_Width-1:0] bwen = ~BWEN;
|
|reg [Bits-1:0] ram [0:Word_Depth-1];
|always @(posedge CLK) begin
|    if(cen && wen) begin
|        ram[A] <= (D & bwen) | (ram[A] & ~bwen);
|    end
|    Q <= cen && !wen ? ram[A] : {4{$random}};
|end
|
|endmodule

                """.stripMargin)
}