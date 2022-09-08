import chisel3._
import chisel3.util._

class ysyx_220154_MEM_DPI extends BlackBox with HasBlackBoxInline
{
    val io = IO(new Bundle
    {
        val clock = Input(Clock())
        val raddr = Input(UInt(64.W))
        val waddr = Input(UInt(64.W))
        val wdata = Input(UInt(64.W))
        val wmask = Input(UInt(8.W))

        val rdata = Output(UInt(64.W))
    })
    setInline("MEM_DPI.v",
                """
|module ysyx_220154_MEM_DPI
|(
|input clock,
|input [63:0]raddr,
|input [63:0]waddr,
|input [63:0]wdata,
|input [7:0]wmask,
|
|output [63:0]rdata
|);
|
|import "DPI-C" function void pmem_read(input longint raddr, output longint rdata);
|import "DPI-C" function void pmem_write(input longint waddr, input longint wdata, input byte wmask);
|
|always @(posedge clock) 
|begin
|   pmem_read(raddr, rdata);
|   pmem_write(waddr, wdata, wmask);
|end
|
|endmodule
                """.stripMargin)
}