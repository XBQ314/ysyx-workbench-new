import chisel3._
import chisel3.util._

class ysyx_040154_LOADUNIT extends BlackBox with HasBlackBoxInline
{
    val io = IO(new Bundle
    {
        val raddr = Input(UInt(3.W))
        val rdata_native = Input(UInt(64.W))
        val LOADctrl = Input(UInt(3.W))

        val rdata = Output(UInt(64.W))
    })
    setInline("LOADUNIT.v",
                """
|module ysyx_040154_LOADUNIT
|(
|input [2:0]raddr,
|input [63:0]rdata_native,
|input [2:0]LOADctrl,
|
|output [63:0]rdata
|);
|
|wire [63:0] lb_out;
|wire [63:0] lbu_out;
|wire [63:0] lh_out;
|wire [63:0] lhu_out;
|wire [63:0] lw_out;
|wire [63:0] lwu_out;
|wire [63:0] ld_out;
|assign rdata = (LOADctrl == 3'b001)?lb_out:
|               (LOADctrl == 3'b010)?lbu_out:
|               (LOADctrl == 3'b011)?lh_out:
|               (LOADctrl == 3'b100)?lhu_out:
|               (LOADctrl == 3'b101)?lw_out:
|               (LOADctrl == 3'b110)?lwu_out:
|               (LOADctrl == 3'b111)?ld_out:'d0;
|
|assign lb_out  = (raddr[2:0]==3'b000)?{{56{rdata_native[7]}}, rdata_native[7:0]}:
|                 (raddr[2:0]==3'b001)?{{56{rdata_native[15]}}, rdata_native[15:8]}:
|                 (raddr[2:0]==3'b010)?{{56{rdata_native[23]}}, rdata_native[23:16]}:
|                 (raddr[2:0]==3'b011)?{{56{rdata_native[31]}}, rdata_native[31:24]}:
|                 (raddr[2:0]==3'b100)?{{56{rdata_native[39]}}, rdata_native[39:32]}:
|                 (raddr[2:0]==3'b101)?{{56{rdata_native[47]}}, rdata_native[47:40]}:
|                 (raddr[2:0]==3'b110)?{{56{rdata_native[55]}}, rdata_native[55:48]}:
|                 (raddr[2:0]==3'b111)?{{56{rdata_native[63]}}, rdata_native[63:56]}:'d0;
|assign lbu_out = (raddr[2:0]==3'b000)?{{56{1'b0}}, rdata_native[7:0]}:
|                 (raddr[2:0]==3'b001)?{{56{1'b0}}, rdata_native[15:8]}:
|                 (raddr[2:0]==3'b010)?{{56{1'b0}}, rdata_native[23:16]}:
|                 (raddr[2:0]==3'b011)?{{56{1'b0}}, rdata_native[31:24]}:
|                 (raddr[2:0]==3'b100)?{{56{1'b0}}, rdata_native[39:32]}:
|                 (raddr[2:0]==3'b101)?{{56{1'b0}}, rdata_native[47:40]}:
|                 (raddr[2:0]==3'b110)?{{56{1'b0}}, rdata_native[55:48]}:
|                 (raddr[2:0]==3'b111)?{{56{1'b0}}, rdata_native[63:56]}:'d0;
|assign lh_out  = (raddr[2:1]==2'b00)?{{48{rdata_native[15]}}, rdata_native[15:0]}:
|                 (raddr[2:1]==2'b01)?{{48{rdata_native[31]}}, rdata_native[31:16]}:
|                 (raddr[2:1]==2'b10)?{{48{rdata_native[47]}}, rdata_native[47:32]}:
|                 (raddr[2:1]==2'b11)?{{48{rdata_native[63]}}, rdata_native[63:48]}:'d0;
|assign lhu_out = (raddr[2:1]==2'b00)?{{48{1'b0}}, rdata_native[15:0]}:
|                 (raddr[2:1]==2'b01)?{{48{1'b0}}, rdata_native[31:16]}:
|                 (raddr[2:1]==2'b10)?{{48{1'b0}}, rdata_native[47:32]}:
|                 (raddr[2:1]==2'b11)?{{48{1'b0}}, rdata_native[63:48]}:'d0;
|assign lw_out  = raddr[2]?{{32{rdata_native[63]}}, rdata_native[63:32]}:
|                           {{32{rdata_native[31]}}, rdata_native[31:0]};
|assign lwu_out = raddr[2]?{{32{1'b0}}, rdata_native[63:32]}:
|                           {{32{1'b0}}, rdata_native[31:0]};
|assign ld_out  = rdata_native;
|endmodule
                """.stripMargin)
}