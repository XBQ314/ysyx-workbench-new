import chisel3._
import chisel3.util._

class TopInterface extends BlackBox with HasBlackBoxInline
{
    val io = IO(new Bundle
    {
        val clock = Input(Clock())
        val reset  = Input(Bool())

        val rw_valid_i  = Input(Bool())
        val enw_i       = Input(Bool())
        val rw_w_data_i = Input(UInt(64.W))
        val rw_addr_i   = Input(UInt(32.W))
        val rw_size_i   = Input(UInt(8.W))

        val rw_ready_o  = Output(Bool())
        val data_read_o = Output(UInt(64.W))

        val mode = Output(UInt(1.W)) // mode=0 -> read; mode -> 1 write;
        val valid = Output(Bool())
        val addr = Output(UInt(64.W))
        val w_data = Output(UInt(8.W))
        val ready = Input(Bool())
        val r_data_valid = Input(Bool())
        val r_data = Input(UInt(8.W))        
    })
    setInline("TopInterface.v",
                """
|module TopInterface
|(
|input clock,
|input reset,
|
|// 与ARBITER交互
|input rw_valid_i,
|input enw_i,
|input [63:0]rw_w_data_i,
|input [31:0]rw_addr_i,
|input [7:0]rw_size_i, // 接了cache之后, 写回都是八字节写回, 掩码是0xff. 如果是外设MMIO, 可能是1. 
|
|output rw_ready_o,
|output [63:0]data_read_o,
|
|// CPU顶层输出信号
|output mode, // mode=0 -> read; mode -> 1 write;
|output valid,
|output [63:0] addr,
|output [7:0] w_data,
|
|input ready,
|input r_data_valid, // 有效一个周期
|input [7:0] r_data
|);
|
|parameter IDLE = 2'b00;
|parameter READ = 2'b01;
|parameter WRIT = 2'b10;
|
|reg [1:0] cur_state;
|reg [1:0] nxt_state;
|
|reg [2:0]cur_tran_num;
|reg [63:0]data_read_o_reg;
|reg rw_ready_o_reg;
|
|always @(posedge clock)
|begin
|    if(reset) cur_state <= 'd0;
|    else cur_state <= nxt_state;
|end
|
|always @(posedge clock)
|begin
|    if(reset)
|    begin
|        cur_tran_num <= 'd0;
|        data_read_o_reg <= 'd0;
|        rw_ready_o_reg <= 'd0;
|    end
|    else if(cur_state == IDLE)
|    begin
|        cur_tran_num <= 'd0;
|        data_read_o_reg <= 'd0;
|        rw_ready_o_reg <= 'd0;
|    end
|    else if(cur_state == READ)
|    begin
|        if(r_data_valid && ready)
|        begin
|            cur_tran_num <= cur_tran_num+'d1;
|            data_read_o_reg <= {r_data, data_read_o_reg[63:8]};
|        end
|
|        // if(r_data_valid && ready && (cur_tran_num=='d7))
|        if (((cur_tran_num == 'd7) || (rw_size_i == 8'h01)) && r_data_valid && ready)
|        begin
|            rw_ready_o_reg <= 1'b1;
|        end
|    end
|    else if(cur_state == WRIT)
|    begin
|        if(ready)
|        begin
|            cur_tran_num <= cur_tran_num+'d1;
|            data_read_o_reg <= 'd0;
|        end
|
|        // if (ready && (cur_tran_num=='d7))
|        if (((cur_tran_num == 'd7 && rw_size_i == 8'hff) || (rw_size_i == 8'h01)) && ready)
|        begin
|            rw_ready_o_reg <= 1'b1;
|        end
|    end
|end
|
|always @*
|begin
|    case (cur_state)
|    IDLE:
|    begin
|        if (rw_valid_i && enw_i)
|        begin
|            nxt_state = WRIT;    
|        end
|        else if (rw_valid_i && !enw_i)
|        begin
|            nxt_state = READ;    
|        end
|        else
|        begin
|            nxt_state = IDLE;    
|        end
|    end
|    READ:
|    begin
|        if (!rw_valid_i || (((cur_tran_num == 'd7) || (rw_size_i == 8'h01)) && r_data_valid && ready))
|        begin
|            nxt_state = IDLE;
|        end
|        else
|        begin
|            nxt_state = READ;
|        end
|    end
|    WRIT: 
|    begin
|        if (!rw_valid_i || (((cur_tran_num == 'd7 && rw_size_i == 8'hff) || (rw_size_i == 8'h01)) && ready))
|        begin
|            nxt_state = IDLE;
|        end
|        else
|        begin
|            nxt_state = WRIT;
|        end
|    end
|    default:
|    begin
|        nxt_state = IDLE;
|    end
|    endcase
|end
|
|// always@*
|// begin
|//     case (cur_state)
|//     IDLE:
|//     begin
|//         if(rw_valid_i && rw_size_i == 8'h01) valid = 'd1;
|//         else valid = 'd0;
|//     end
|//     READ:
|//     begin
|//         // if (!rw_valid_i || (((cur_tran_num == 'd7 && rw_size_i == 8'hff) || (rw_size_i == 8'h01)) && ready)) valid = 'd0;
|//         // else valid = 'd1;
|//         valid = 'd1;
|//     end
|//     WRIT: 
|//     begin
|//         // if (!rw_valid_i || (((cur_tran_num == 'd7 && rw_size_i == 8'hff) || (rw_size_i == 8'h01)) && ready)) valid = 'd0;
|//         // else valid = 'd1;
|//         valid = 'd1;
|//     end
|//     default:
|//     begin
|//         valid = 'd0;
|//     end
|//     endcase
|// end
|
|assign data_read_o = data_read_o_reg;
|assign rw_ready_o = rw_ready_o_reg;
|
|assign mode = enw_i;
|assign valid = (cur_state == READ)||(cur_state == WRIT);
|assign addr = {32'd0, rw_addr_i[31:3], cur_tran_num};
|assign w_data = (cur_tran_num==3'd0)?rw_w_data_i[7 :0]:
|                (cur_tran_num==3'd1)?rw_w_data_i[15:8]:
|                (cur_tran_num==3'd2)?rw_w_data_i[23:16]:
|                (cur_tran_num==3'd3)?rw_w_data_i[31:24]:
|                (cur_tran_num==3'd4)?rw_w_data_i[39:32]:
|                (cur_tran_num==3'd5)?rw_w_data_i[47:40]:
|                (cur_tran_num==3'd6)?rw_w_data_i[55:48]:
|                (cur_tran_num==3'd7)?rw_w_data_i[63:56]:
|                'd0;
|endmodule
                """.stripMargin)
}