import chisel3._
import chisel3.util._

class ysyx_220154_IFU_DPI extends BlackBox with HasBlackBoxInline
{
    val io = IO(new Bundle
    {
        val pc = Input(UInt(64.W))
        val inst = Output(UInt(32.W))
        val raw_data = Output(UInt(64.W))
    })
    setInline("IFU_DPI.v",
                """
|module ysyx_220154_IFU_DPI
|(
|    input [63:0]pc,
|    output [31:0]inst,
|    output [63:0]raw_data
|);
|
|import "DPI-C" function void pmem_read(input longint raddr, output longint rdata);
|
|wire[63:0]rdata;
|
|always@(*)
|begin
|    pmem_read(pc, rdata);
|end
|
|assign inst = pc[2]?rdata[63:32]:rdata[31:0];
|assign raw_data = rdata;
|
|endmodule
                """.stripMargin)
}