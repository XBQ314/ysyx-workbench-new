import chisel3._
import chisel3.util._

class DCACHE extends BlackBox with HasBlackBoxInline
{
    val io = IO(new Bundle
    {
        val clk = Input(Clock())
        val reset = Input(Bool())

        val index = Input(UInt(8.W))
        val enw = Input(Bool())
        val tag_enw = Input(Bool())
        val wdata = Input(UInt(128.W))
        val wmask = Input(UInt(128.W))
        val in_valid = Input(UInt(1.W))
        val in_dirty = Input(UInt(1.W))
        val in_tag = Input(UInt(52.W))

        val data = Output(UInt(128.W))
        val valid = Output(UInt(1.W))
        val dirty = Output(UInt(1.W))
        val tag = Output(UInt(52.W))
    })
    setInline("DCACHE.v",
                """
|module DCACHE
|(
|    input clk,
|    input reset,
|
|    input [7:0]index,
|    input enw,
|    input tag_enw,
|    input [127:0]wdata,
|    input [127:0]wmask,
|    input in_valid,
|    input in_dirty,
|    input [51:0]in_tag,
|
|    output reg[127:0]data,
|    output valid,
|    output dirty,
|    output [51:0]tag
|);
|
|reg ram0_cen;
|reg ram1_cen;
|reg ram2_cen;
|reg ram3_cen;
|wire [127:0]ram0_Q;
|wire [127:0]ram1_Q;
|wire [127:0]ram2_Q;
|wire [127:0]ram3_Q;
|
|reg [51:0]tag_vec[255:0];
|reg valid_vec[255:0];
|reg dirty_vec[255:0];
|
|assign valid = valid_vec[index];
|assign dirty = dirty_vec[index];
|assign tag = tag_vec[index];
|
|S011HD1P_X32Y2D128_BW RAM0(.CLK(clk), .CEN(ram0_cen), .WEN(!enw), .BWEN(~wmask), .A(index[5:0]), .D(wdata), .Q(ram0_Q));
|S011HD1P_X32Y2D128_BW RAM1(.CLK(clk), .CEN(ram1_cen), .WEN(!enw), .BWEN(~wmask), .A(index[5:0]), .D(wdata), .Q(ram1_Q));
|S011HD1P_X32Y2D128_BW RAM2(.CLK(clk), .CEN(ram2_cen), .WEN(!enw), .BWEN(~wmask), .A(index[5:0]), .D(wdata), .Q(ram2_Q));
|S011HD1P_X32Y2D128_BW RAM3(.CLK(clk), .CEN(ram3_cen), .WEN(!enw), .BWEN(~wmask), .A(index[5:0]), .D(wdata), .Q(ram3_Q));
|
|reg [7:0]index_reg;
|
|always @(posedge clk)
|begin
|    if(reset) index_reg<='d0;
|    else index_reg <= index;    
|end
|
|always @(posedge clk)
|begin
|    if(reset)
|    begin
|        valid_vec[index]<='d0;
|        dirty_vec[index]<='d0;
|    end
|    else
|    begin
|        valid_vec[index]<=tag_enw?in_valid:valid_vec[index];
|        dirty_vec[index]<=tag_enw?in_dirty:dirty_vec[index];
|    end
|end
|
|// TAG不需要复位
|always @(posedge clk)
|begin
|    tag_vec[index]<=tag_enw?in_tag:tag_vec[index];
|end
|
|always @*
|begin
|    case(index_reg[7:6])
|    2'b00:
|    begin
|        ram0_cen=1'b0;
|        ram1_cen=1'b1;
|        ram2_cen=1'b1;
|        ram3_cen=1'b1;
|        data=ram0_Q;
|    end
|    2'b01:
|    begin
|        ram0_cen=1'b1;
|        ram1_cen=1'b0;
|        ram2_cen=1'b1;
|        ram3_cen=1'b1;
|        data=ram1_Q;
|    end
|    2'b10:
|    begin
|        ram0_cen=1'b1;
|        ram1_cen=1'b1;
|        ram2_cen=1'b0;
|        ram3_cen=1'b1;
|        data=ram2_Q;
|    end
|    2'b11:
|    begin
|        ram0_cen=1'b1;
|        ram1_cen=1'b1;
|        ram2_cen=1'b1;
|        ram3_cen=1'b0;
|        data=ram3_Q;
|    end
|    endcase    
|end
|
|endmodule
                """.stripMargin)
}
// class DCACHE extends Module
// {
//     val io = IO(new Bundle
//     {
//         val CLK = Input(Clock())

//         val index = Input(UInt(8.W))
//         val enw = Input(Bool())
//         val tag_enw = Input(Bool())
//         val wdata = Input(UInt(128.W))
//         val wmask = Input(UInt(128.W))
//         val in_valid = Input(UInt(1.W))
//         val in_dirty = Input(UInt(1.W))
//         val in_tag = Input(UInt(52.W))

//         val data = Output(UInt(128.W))
//         val valid = Output(UInt(1.W))
//         val dirty = Output(UInt(1.W))
//         val tag = Output(UInt(52.W))

//         // // TOP SRAM IO
//         // val sram4_addr      = Output(UInt(6.W))
//         // val sram4_cen       = Output(UInt(1.W))
//         // val sram4_wen       = Output(UInt(1.W))
//         // val sram4_wmask     = Output(UInt(128.W))
//         // val sram4_wdata     = Output(UInt(128.W))
//         // val sram4_rdata     = Input(UInt(128.W))

//         // val sram5_addr      = Output(UInt(6.W))
//         // val sram5_cen       = Output(UInt(1.W))
//         // val sram5_wen       = Output(UInt(1.W))
//         // val sram5_wmask     = Output(UInt(128.W))
//         // val sram5_wdata     = Output(UInt(128.W))
//         // val sram5_rdata     = Input(UInt(128.W))

//         // val sram6_addr      = Output(UInt(6.W))
//         // val sram6_cen       = Output(UInt(1.W))
//         // val sram6_wen       = Output(UInt(1.W))
//         // val sram6_wmask     = Output(UInt(128.W))
//         // val sram6_wdata     = Output(UInt(128.W))
//         // val sram6_rdata     = Input(UInt(128.W))

//         // val sram7_addr      = Output(UInt(6.W))
//         // val sram7_cen       = Output(UInt(1.W))
//         // val sram7_wen       = Output(UInt(1.W))
//         // val sram7_wmask     = Output(UInt(128.W))
//         // val sram7_wdata     = Output(UInt(128.W))
//         // val sram7_rdata     = Input(UInt(128.W))
//     })
//     val RAM0 = Module(new S011HD1P_X32Y2D128_BW())
//     val RAM1 = Module(new S011HD1P_X32Y2D128_BW())
//     val RAM2 = Module(new S011HD1P_X32Y2D128_BW())
//     val RAM3 = Module(new S011HD1P_X32Y2D128_BW())

//     // 默认值
//     RAM0.io.CLK := io.CLK
//     RAM0.io.CEN := 1.U
//     RAM0.io.WEN := !io.enw
//     RAM0.io.BWEN:= ~io.wmask
//     RAM0.io.A   := io.index(5, 0)
//     RAM0.io.D   := io.wdata
//     // io.sram4_addr   := io.index(5, 0)
//     // io.sram4_cen    := 1.U
//     // io.sram4_wen    := !io.enw
//     // io.sram4_wmask  := ~io.wmask
//     // io.sram4_wdata  := io.wdata

//     RAM1.io.CLK := io.CLK
//     RAM1.io.CEN := 1.U
//     RAM1.io.WEN := !io.enw
//     RAM1.io.BWEN:= ~io.wmask
//     RAM1.io.A   := io.index(5, 0)
//     RAM1.io.D   := io.wdata
//     // io.sram5_addr   := io.index(5, 0)
//     // io.sram5_cen    := 1.U
//     // io.sram5_wen    := !io.enw
//     // io.sram5_wmask  := ~io.wmask
//     // io.sram5_wdata  := io.wdata

//     RAM2.io.CLK := io.CLK
//     RAM2.io.CEN := 1.U
//     RAM2.io.WEN := !io.enw
//     RAM2.io.BWEN:= ~io.wmask
//     RAM2.io.A   := io.index(5, 0)
//     RAM2.io.D   := io.wdata
//     // io.sram6_addr   := io.index(5, 0)
//     // io.sram6_cen    := 1.U
//     // io.sram6_wen    := !io.enw
//     // io.sram6_wmask  := ~io.wmask
//     // io.sram6_wdata  := io.wdata

//     RAM3.io.CLK := io.CLK
//     RAM3.io.CEN := 1.U
//     RAM3.io.WEN := !io.enw
//     RAM3.io.BWEN:= ~io.wmask
//     RAM3.io.A   := io.index(5, 0)
//     RAM3.io.D   := io.wdata
//     // io.sram7_addr   := io.index(5, 0)
//     // io.sram7_cen    := 1.U
//     // io.sram7_wen    := !io.enw
//     // io.sram7_wmask  := ~io.wmask
//     // io.sram7_wdata  := io.wdata

//     val io_index = RegNext(io.index, 0.U(8.W))
//     io.data := 0.U
//     switch(io_index(7, 6))
//     {
//         is("b00".U)
//         {
//             RAM0.io.CEN := 0.U
//             io.data := RAM0.io.Q
//             // io.sram4_cen := 0.U
//             // io.data := io.sram4_rdata
//         }
//         is("b01".U)
//         {
//             RAM1.io.CEN := 0.U
//             io.data := RAM1.io.Q
//             // io.sram5_cen := 0.U
//             // io.data := io.sram5_rdata
//         }
//         is("b10".U)
//         {
//             RAM2.io.CEN := 0.U
//             io.data := RAM2.io.Q
//             // io.sram6_cen := 0.U
//             // io.data := io.sram6_rdata
//         }
//         is("b11".U)
//         {
//             RAM3.io.CEN := 0.U
//             io.data := RAM3.io.Q
//             // io.sram7_cen := 0.U
//             // io.data := io.sram7_rdata
//         }
//     }

//     val TAG_VEC = RegInit(VecInit(Seq.fill(256)(0.U(54.W))))
//     // val TAG_VEC = RegEnable(Cat(io.in_valid, io.in_dirty, io.in_tag), VecInit(Seq.fill(256)(0.U(54.W))), io.enMEM2WB)
//     io.valid := TAG_VEC(io.index)(53)
//     io.dirty := TAG_VEC(io.index)(52)
//     io.tag   := TAG_VEC(io.index)(51,0)
//     TAG_VEC(io.index)  := Mux(io.tag_enw, Cat(io.in_valid, io.in_dirty, io.in_tag), TAG_VEC(io.index))
// }