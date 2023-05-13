import chisel3._
import chisel3.util._

class ysyx_040154_DIV extends BlackBox with HasBlackBoxInline
{
    val io = IO(new Bundle
    {
        val clock = Input(Clock())
        val reset  = Input(Bool())

        val div_valid = Input(Bool())
        val flush = Input(Bool())
        val div_signed = Input(Bool())

        val dividend = Input(UInt(64.W))
        val divisor = Input(UInt(64.W))

        val div_ready = Output(Bool())
        val out_valid = Output(Bool())

        val quotient = Output(UInt(64.W))
        val remainder = Output(UInt(64.W))

    })
    setInline("DIV.v",
            """
|module ysyx_040154_DIV
|(
|    input clock,
|    input reset,
|
|    input [63:0] dividend, //被除数
|    input [63:0] divisor, //除数
|
|    input div_valid, //为高表示输入的数据有效,如果没有新的除法输入,在除法被接受的下一个周期要置低
|    input div_signed, //表示是不是有符号除法,为高表示是有符号除法
|    input flush, //为高表示要取消除法(修改一下除法器状态就行)
|    
|    output reg div_ready, //为高表示除法器空闲,可以输入数据
|    output reg out_valid, //为高表示除法器输出了有效结果
|
|    output [63:0] quotient, //商
|    output [63:0] remainder //余数
|);
|parameter IDLE = 2'b00;
|parameter BUSY = 2'b01;
|parameter DONE = 2'b10;
|
|reg [1:0] cur_state;
|reg [1:0] nxt_state;
|
|reg [127:0] dividend_reg;
|reg [63:0] divisor_reg;
|reg [6:0] cnt;
|reg done_flag;
|
|reg [63:0] quotient_reg;
|reg [63:0] remainder_reg;
|reg quotient_sign_reg;
|reg remainder_sign_reg;
|wire dividend_sign;
|wire divisor_sign;
|assign dividend_sign = dividend[63];
|assign divisor_sign = divisor[63];
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
|    if(cur_state == IDLE)
|    begin
|        if(div_valid) nxt_state = BUSY;
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
|    if(cur_state == IDLE)
|    begin
|        div_ready = 1'b1;
|        out_valid = 1'b0;
|    end
|    else if(cur_state == BUSY)
|    begin
|        div_ready = 1'b0;
|        out_valid = 1'b0;
|    end
|    else if(cur_state == DONE)
|    begin
|        div_ready = 1'b0;
|        out_valid = 1'b1;
|    end
|    else
|    begin
|        div_ready = 1'b0;
|        out_valid = 1'b0;
|    end
|end
|
|wire [64:0]tmp;
|assign tmp = dividend_reg[127:63] - {1'b0, divisor_reg};
|always@(posedge clock)
|begin
|    if(reset)
|    begin
|        quotient_reg        <= 'd0;
|        remainder_reg       <= 'd0;
|        dividend_reg        <= 'd0;
|        divisor_reg         <= 'd0;
|        quotient_sign_reg   <= 'd0;
|        remainder_sign_reg  <= 'd0;
|        cnt                 <= 'd0;
|        done_flag           <= 'd0;
|    end
|    else if(cur_state == IDLE || flush)
|    begin
|        quotient_reg        <= 'd0;
|        remainder_reg       <= 'd0;
|        dividend_reg        <= 'd0;
|        divisor_reg         <= 'd0;
|        quotient_sign_reg   <= 'd0;
|        remainder_sign_reg  <= 'd0;
|        cnt                 <= 'd0;
|        done_flag           <= 'd0;
|    end
|    else if(cur_state == BUSY)
|    begin
|        cnt <= cnt+'d1;
|        if(cnt == 'd0)
|        begin
|            dividend_reg        <= {64'd0, div_signed?
|                                           dividend_sign?~dividend+1'b1:dividend
|                                           :dividend};
|            divisor_reg         <= div_signed?
|                                   divisor_sign?~divisor+1'b1:divisor
|                                   :divisor;
|            quotient_sign_reg   <= div_signed?dividend_sign^divisor_sign:1'b0;
|            remainder_sign_reg  <= div_signed?dividend_sign:1'b0;
|        end
|        else
|        begin
|            quotient_reg <= quotient_reg << 1;
|            quotient_reg[0] <= !tmp[64];
|            dividend_reg <= tmp[64]?dividend_reg<<1:{tmp[63:0], dividend_reg[62:0], 1'b0};
|            remainder_reg <= tmp[64]?dividend_reg[126:63]:tmp[63:0];
|            if(cnt == 'd63)
|            begin
|                done_flag <= 'd1;
|            end
|            else done_flag <= 'd0;
|        end
|    end
|end
|
|assign quotient = quotient_sign_reg?~quotient_reg+1'b1:quotient_reg;
|assign remainder = remainder_sign_reg?~remainder_reg+1'b1:remainder_reg;
|endmodule
|
                """.stripMargin)
}