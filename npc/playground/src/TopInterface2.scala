import chisel3._
import chisel3.util._

class TopInterface2 extends BlackBox with HasBlackBoxInline
{
    val io = IO(new Bundle
    {
        val clock = Input(Clock())
        val reset  = Input(Bool())

        val inter1_mode  = Input(UInt(1.W))
        val inter1_valid = Input(Bool())
        val inter1_addr = Input(UInt(64.W))
        val inter1_w_data = Input(UInt(8.W))

        val ready2inter1  = Output(Bool())
        val r_data_valid2inter1 = Output(Bool())
        val r_data2inter1 = Output(UInt(8.W))

        val mode = Output(UInt(1.W))
        val ready = Input(UInt(1.W))
        val w_valid = Output(UInt(1.W))
        val w_data = Output(UInt(8.W))
        val r_valid = Input(UInt(1.W))
        val r_data = Input(UInt(8.W))
    })
    setInline("TopInterface2.v",
                """
|module TopInterface2
|(
|input clock,
|input reset,
|
|// TopInterface1的信号
|input inter1_mode, // mode=0 -> read; mode -> 1 write;
|input inter1_valid,
|input [63:0] inter1_addr,
|input [7:0] inter1_w_data,
|
|output ready2inter1,
|output r_data_valid2inter1, // 有效一个周期
|output [7:0] r_data2inter1,
|
|// cpu顶层信号
|output reg mode,
|input ready,
|output reg w_valid,
|output [7:0]w_data,
|input r_valid,
|input [7:0]r_data
|);
|parameter IDLE = 2'b00;
|parameter READ = 2'b01;
|parameter WRIT = 2'b10;
|
|reg [1:0] cur_state;
|reg [1:0] nxt_state;
|
|reg [3:0] cnt;
|always @(posedge clock)
|begin
|    if(reset) cur_state <= 'd0;
|    else cur_state <= nxt_state;
|end
|
|always @*
|begin
|    case (cur_state)
|    IDLE:
|    begin
|        if(inter1_mode & inter1_valid & ready) nxt_state=WRIT;
|        else if(!inter1_mode & inter1_valid & ready) nxt_state=READ;
|        else nxt_state=IDLE;
|    end
|    READ:
|    begin
|        if(r_valid) nxt_state=IDLE;
|        else nxt_state=READ;
|    end
|    WRIT: 
|    begin
|        if(cnt == 'd9 && ready) nxt_state=IDLE;
|        else nxt_state=WRIT;
|    end
|    default:
|    begin
|        nxt_state = IDLE;
|    end
|    endcase
|end
|
|always @(posedge clock)
|begin
|    if(reset)
|    begin
|        mode<='d0;
|        w_valid<='d0;
|        cnt<='d0;
|    end
|    else
|    begin
|        case (cur_state)
|        IDLE:
|        begin
|            mode<=(inter1_mode & inter1_valid & ready)?1'b1:1'b0;
|            w_valid<=(inter1_valid & ready)?1'b1:1'b0;
|            cnt<='d0;
|        end
|        READ:
|        begin
|            mode<='d0;
|            w_valid<=(cnt=='d7)?1'b0:1'b1;
|            cnt<=(cnt=='d7)?'d7:cnt+1'b1;
|        end
|        WRIT: 
|        begin
|            mode<=1'b1;
|            w_valid<=(cnt>='d8)?1'b0:1'b1;
|            cnt<=(cnt=='d9)?'d9:cnt+1'b1;
|        end 
|        endcase
|    end    
|end
|
|assign ready2inter1=((cur_state==READ)&&r_valid)||((cur_state==WRIT)&&cnt=='d9 && ready);
|assign r_data_valid2inter1=r_valid;
|assign r_data2inter1=r_data;
|assign w_data=(cnt=='d0)?inter1_addr[7 :0]:
|              (cnt=='d1)?inter1_addr[15:8]:
|              (cnt=='d2)?inter1_addr[23:16]:
|              (cnt=='d3)?inter1_addr[31:24]:
|              (cnt=='d4)?inter1_addr[39:32]:
|              (cnt=='d5)?inter1_addr[47:40]:
|              (cnt=='d6)?inter1_addr[55:48]:
|              (cnt=='d7)?inter1_addr[63:56]:
|              (cnt=='d8)?inter1_w_data:'d0;
|              
|endmodule
                """.stripMargin)
}