import chisel3._
import chisel3.util._

class ysyx_220154_MUL extends BlackBox with HasBlackBoxInline
{
    val io = IO(new Bundle
    {
        val clock = Input(Clock())
        val reset  = Input(Bool())

        val mul_valid = Input(Bool())
        val flush = Input(Bool())
        val mulw = Input(Bool())
        val mul_signed = Input(UInt(2.W))

        val multiplicand = Input(UInt(64.W))
        val multipiler = Input(UInt(64.W))

        val mul_ready = Output(Bool())
        val out_valid = Output(Bool())

        val result_hi = Output(UInt(64.W))
        val result_lo = Output(UInt(64.W))

    })
    setInline("MUL.v",
            """
|module ysyx_220154_MUL
|(
|    input clock,
|    input reset,
|
|    input mul_valid, //为高表示输入的数据有效,如果没有新的乘法输入,在乘法被接受的下一个周期要置低
|    input flush, //为高表示取消乘法
|    input mulw, //为高表示是 32 位乘法
|    input [1:0] mul_signed, //2'b11(signed x signed); 2'b10(signed x unsigned); 2'b00(unsigned x unsigned);
|
|    input [63:0] multiplicand, //被乘数,就是第一个数
|    input [63:0] multipiler, //乘数,就是第二个数
|
|    output reg mul_ready, //为高表示乘法器准备好,表示可以输入数据
|    output reg out_valid, //为高表示乘法器输出的结果有效
|
|    output [63:0] result_hi,
|    output [63:0] result_lo
|);
|
|parameter IDLE = 2'b00;
|parameter BUSY = 2'b01;
|parameter DONE = 2'b10;
|
|reg [1:0] cur_state;
|reg [1:0] nxt_state;
|
|reg [127:0] result;
|reg [63:0] multiplicand_reg;
|reg [63:0] multipiler_reg;
|reg [6:0] cnt;
|reg done_flag;
|
|always@(posedge	clock)
|begin
|	if(reset) cur_state<=IDLE;
|	else if(flush) cur_state <= IDLE;
|    else
|    begin
|        cur_state<=nxt_state;
|    end
|end
|
|always@(*)
|begin
|    // case(cur_state)
|    //     IDLE:
|    //     begin
|    //         if(mul_valid) nxt_state = BUSY;
|    //         else nxt_state = IDLE;
|    //     end
|    //     BUSY:
|    //     begin
|    //         if(done_flag) nxt_state = DONE;
|    //         else nxt_state = BUSY;
|    //     end
|    //     DONE:
|    //     begin
|    //         nxt_state = IDLE;
|    //     end
|    // endcase
|    if(cur_state == IDLE)
|    begin
|        if(mul_valid) nxt_state = BUSY;
|        else nxt_state = IDLE;
|    end
|    else if(cur_state == BUSY)
|    begin
|        if(done_flag) nxt_state = DONE;
|        else nxt_state = BUSY;
|    end
|    else if(cur_state == DONE)
|    begin
|        nxt_state = IDLE;
|    end
|    else nxt_state = cur_state;
|end
|
|always@(*)
|begin
|    // case(cur_state)
|    //     IDLE:
|    //     begin
|    //         mul_ready = 1'b1;
|    //         out_valid = 1'b0;
|    //     end
|    //     BUSY:
|    //     begin
|    //         mul_ready = 1'b0;
|    //         out_valid = 1'b0;
|    //     end
|    //     DONE:
|    //     begin
|    //         mul_ready = 1'b0;
|    //         out_valid = 1'b1;
|    //     end
|    // endcase
|    if(cur_state == IDLE)
|    begin
|        mul_ready = 1'b1;
|        out_valid = 1'b0;
|    end
|    else if(cur_state == BUSY)
|    begin
|        mul_ready = 1'b0;
|        out_valid = 1'b0;
|    end
|    else if(cur_state == DONE)
|    begin
|        mul_ready = 1'b0;
|        out_valid = 1'b1;
|    end
|    else
|    begin
|        mul_ready = 1'b0;
|        out_valid = 1'b0;
|    end
|end
|
|always@(posedge clock)
|begin
|    if(reset)
|    begin
|        result              <= 'd0;
|        multiplicand_reg    <= 'd0;
|        multipiler_reg      <= 'd0;
|        cnt                 <= 'd0;
|        done_flag           <= 'd0;
|    end
|    else if(cur_state == IDLE || flush)
|    begin
|        result              <= 'd0;
|        multiplicand_reg    <= 'd0;
|        multipiler_reg      <= 'd0;
|        cnt                 <= 'd0;
|        done_flag           <= 'd0;
|    end
|    else if(cur_state == BUSY)
|    begin
|        cnt <= cnt+'d1;
|        if(cnt == 'd0)
|        begin
|            multiplicand_reg <= multiplicand;
|            multipiler_reg <= multipiler;
|        end
|        else
|        begin
|            multipiler_reg <= multipiler_reg >> 1;
|            result <= result + (multipiler_reg[0]?{64'h0, multiplicand_reg}<<(cnt-1):'d0);
|            if(multipiler_reg == 'd0)done_flag <= 'd1;
|            else done_flag <= 'd0;
|        end
|    end
|end
|
|assign result_hi = result[127:64];
|assign result_lo = result[63:0];
|
|endmodule
|
                """.stripMargin)
}