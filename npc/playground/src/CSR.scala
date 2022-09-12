import chisel3._
import chisel3.util._

// class CSR extends Module
// {
//     val io = IO(new Bundle
//     {
//         val read_idx = Input(UInt(8.W))

//         val write_idx = Input(UInt(8.W))
//         val in_data = Input(UInt(64.W))
//         val enw = Input(Bool())

//         val csr_out = Output(UInt(64.W))
//     })
//     val mstatus = RegInit("ha00001800".U(64.W)) // 0x300
//     val mtvec   = RegInit(0.U(64.W)) // 0x305
//     val mepc    = RegInit(0.U(64.W)) // 0x341
//     val mcause  = RegInit(0.U(64.W)) // 0x342
//     // write
//     when(io.enw)
//     {
//         switch(io.write_idx)
//         {
//         is("h00".U){mstatus := io.in_data}
//         is("h05".U){mtvec   := io.in_data}
//         is("h41".U){mepc    := io.in_data}
//         is("h42".U){mcause  := io.in_data}
//         }
//     }.otherwise
//     {
//         mstatus:=mstatus
//         mtvec:=mtvec
//         mepc:=mepc
//         mcause:=mcause
//     }

//     // read
//     io.csr_out := 0.U
//     switch(io.read_idx)
//     {
//     is("h00".U){io.csr_out := mstatus}
//     is("h05".U){io.csr_out := mtvec}
//     is("h41".U){io.csr_out := mepc}
//     is("h42".U){io.csr_out := mcause}
//     }
// }

class ysyx_220154_CSR extends BlackBox with HasBlackBoxInline
{
    val io = IO(new Bundle
    {
        val clock = Input(Clock())
        val reset  = Input(Bool())
        val read_idx = Input(UInt(8.W))

        val write_idx = Input(UInt(8.W))
        val in_data = Input(UInt(64.W))
        val enw = Input(Bool())

        // val ecall_mepc = Input(UInt(64.W))
        // val ecall_mcause = Input(UInt(64.W))
        // val ecall_flag = Input(Bool())
        val clint_enw = Input(Bool())
        val mstatus_in = Input(UInt(64.W))
        val mepc_in = Input(UInt(64.W))
        val mcause_in = Input(UInt(64.W))
        val mip_in = Input(UInt(64.W))

        val global_int_en = Output(Bool())
        val mstatus_out = Output(UInt(64.W))
        val mtvec_out = Output(UInt(64.W))
        val mepc_out = Output(UInt(64.W))
        val mip_out = Output(UInt(64.W))
        val mie_out = Output(UInt(64.W))
        val csr_out = Output(UInt(64.W))
    })
    setInline("CSR.v",
                """
|module ysyx_220154_CSR
|(
|    input clock,
|    input reset,
|    input [7:0]read_idx,
|    
|    input [7:0]write_idx,
|    input [63:0]in_data,
|    input enw,
|
|    //input [63:0]ecall_mepc,
|    //input [63:0]ecall_mcause,
|    //input ecall_flag,
|   
|    input clint_enw,
|    input [63:0]mstatus_in,
|    input [63:0]mepc_in,
|    input [63:0]mcause_in,
|    input [63:0]mip_in,
|
|    output global_int_en,
|    output [63:0]mstatus_out,
|    output [63:0]mtvec_out,
|    output [63:0]mepc_out,
|    output [63:0]mip_out,
|    output [63:0]mie_out,
|    output [63:0]csr_out
|);
|reg [63:0] mstatus;
|reg [63:0] mie    ;
|reg [63:0] mtvec  ;
|reg [63:0] mepc   ;
|reg [63:0] mcause ;
|reg [63:0] mip    ;
|
|// import "DPI-C" function void read_mstatus(input longint a);
|// import "DPI-C" function void read_mtvec(input longint a);
|// import "DPI-C" function void read_mepc(input longint a);
|// import "DPI-C" function void read_mcause(input longint a);
|// 
|// always@(*)
|// begin
|//     read_mstatus(mstatus);
|//     read_mtvec(mtvec);
|//     read_mepc(mepc);
|//     read_mcause(mcause);
|// end
|
|always@(posedge clock)
|begin
|    if(reset)
|    begin
|        mstatus<=64'ha0001800;
|        mie<='d0;
|        mtvec<='d0;
|        mepc<='d0;
|        mcause<='d0;
|        mip<='d0;
|    end
|    else
|    begin
|        if(enw || clint_enw) 
|        begin
|            mstatus <=(clint_enw == 'd1)?mstatus_in:
|                      (write_idx == 8'h00)?in_data:mstatus;
|            mie     <=(clint_enw == 'd1)?mie:
|                      (write_idx == 8'h04)?in_data:mie;
|            mtvec   <=(clint_enw == 'd1)?mtvec:
|                      (write_idx == 8'h05)?in_data:mtvec;
|            mepc    <=(clint_enw == 'd1)?(|mepc_in)?mepc_in:mepc:
|                      (write_idx == 8'h41)?in_data:mepc;
|            mcause  <=(clint_enw == 'd1)?mcause_in:
|                      (write_idx == 8'h42)?in_data:mcause;
|            mip     <=(clint_enw == 'd1)?mip_in:
|                      (write_idx == 8'h44)?in_data:mip;
|        end
|    end
|end
|
|assign global_int_en = mstatus[3];
|assign mstatus_out = mstatus;
|assign mtvec_out = mtvec;
|assign mepc_out = mepc;
|assign mip_out = mip;
|assign mie_out = mie;
|assign csr_out = (read_idx == 8'h00)?mstatus:
|                 (read_idx == 8'h05)?mtvec:
|                 (read_idx == 8'h41)?mepc:
|                 (read_idx == 8'h42)?mcause:'d0;
|
|endmodule
|
                """.stripMargin)
}