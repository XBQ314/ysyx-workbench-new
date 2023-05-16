import chisel3._
import chisel3.util._

class main_memory extends BlackBox with HasBlackBoxInline
{
    val io = IO(new Bundle
    {
        val clock = Input(Clock())
        val reset  = Input(Bool())

        val mode = Input(UInt(1.W)) // mode=0 -> read; mode -> 1 write;
        val valid = Input(Bool())
        val ready = Output(Bool())
        val addr = Input(UInt(64.W))
        val w_data = Input(UInt(8.W))
        val r_data_valid = Output(Bool())
        val r_data = Output(UInt(8.W))

        val invalid_addr = Output(Bool())
        val write_char_io = Output(Bool())
        val simulation_stop = Output(Bool())
    })
    setInline("main_memory.v",
                """
|// Principles and Design of Microprocessors (PDMP)
|// Fake main memory for simulation
|// Wenbo Guo
|// 2023.03.14
|
|module main_memory(
|    input clock, // posedge active
|    input reset, // high active
|
|    // read/write channel
|    input           mode, // mode=0 -> read; mode -> 1 write;
|    input           valid,
|    output          ready,
|    input  [63:0]   addr,
|    input  [ 7:0]   w_data,
|    output          r_data_valid,
|    output reg [ 7:0]   r_data,
|
|    // Interact with simulation
|    output          invalid_addr, // invalid memory access
|    output          write_char_io, // write a character into address 0x1000
|    output          simulation_stop // end the similation, i.e., write 0xff into address 0x2000
|);
|
|import "DPI-C" function void pmem_read(input longint addr, output byte r_data);
|import "DPI-C" function void pmem_write(input longint addr, input byte w_data);
|
|
|    // measure the simulation status
|    wire read_addr_is_valid = (~mode) & (addr[63:16] == 48'h8000);
|    wire write_addr_is_valid = mode & ((addr[63:16] == 48'h8000) | (addr == 64'h1000) | (addr == 64'h2000));
|    assign invalid_addr = valid & ((~read_addr_is_valid) & (~write_addr_is_valid));
|    assign write_char_io = valid & mode & addr == 64'h1000;
|    assign simulation_stop = valid & mode & addr == 64'h2000 & w_data == 8'hff;
|
|    // read memory
|    assign r_data_valid = valid & (~mode);
|    // assign r_data = {8{read_addr_is_valid & valid}} & mem[addr[15:0]];
|
|    always@*
|    begin
|        if(read_addr_is_valid & valid)
|        begin
|            pmem_read(addr, r_data);
|        end
|        else 
|        begin
|            r_data = 0;
|        end
|    end
|    // write memoey
|    // always @(posedge clock) begin
|    //     if(valid & mode & (addr[63:16] == 48'h8000)) begin
|    //         mem[addr[15:0]] <= w_data;
|    //     end
|    // end
|    always @(posedge clock) begin
|        if(valid & mode)
|        begin
|            pmem_write(addr, w_data);
|        end
|    end
|
|    // reg [7:0] char_0x1000;
|    // always @(posedge clock) begin
|    //     if(valid & mode & (addr == 64'h1000)) begin
|    //         char_0x1000 <= w_data;
|    //         $write("%c", w_data);
|    //     end
|    // end
|
|    assign ready = 1'b1;
|
|
|endmodule
                """.stripMargin)
}