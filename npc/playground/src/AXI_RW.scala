import chisel3._
import chisel3.util._
import xbqpackage._

class ysyx_040154_axi_rw extends BlackBox with HasBlackBoxInline
{
    val io = IO(new Bundle
    {
        val clock = Input(Clock())
        val reset  = Input(Bool())

        val rw_valid_i = Input(Bool())
        val enw_i = Input(Bool())
        val rw_ready_o = Output(Bool())
        val data_read_o = Output(UInt(64.W))
        val rw_w_data_i = Input(UInt(64.W))
        val rw_addr_i = Input(UInt(32.W))
        val rw_size_i = Input(UInt(8.W))

        // Advanced eXtensible Interface
        val axi_aw_ready_i = Input(UInt(1.W))
        val axi_aw_valid_o = Output(UInt(1.W))
        val axi_aw_addr_o = Output(UInt(32.W))
        val axi_aw_prot_o = Output(UInt(3.W))
        val axi_aw_id_o = Output(UInt(4.W))
        val axi_aw_user_o = Output(UInt(1.W))
        val axi_aw_len_o = Output(UInt(8.W))
        val axi_aw_size_o = Output(UInt(3.W))
        val axi_aw_burst_o = Output(UInt(2.W))
        val axi_aw_lock_o = Output(UInt(1.W))
        val axi_aw_cache_o = Output(UInt(4.W))
        val axi_aw_qos_o = Output(UInt(4.W))
        val axi_aw_region_o = Output(UInt(4.W))


        val axi_w_ready_i = Input(UInt(1.W))
        val axi_w_valid_o = Output(UInt(1.W))
        val axi_w_data_o = Output(UInt(64.W))
        val axi_w_strb_o = Output(UInt(8.W))
        val axi_w_last_o = Output(UInt(1.W))
        val axi_w_user_o = Output(UInt(1.W))


        val axi_b_ready_o = Output(UInt(1.W)) 
        val axi_b_valid_i = Input(UInt(1.W))
        val axi_b_resp_i = Input(UInt(2.W))
        val axi_b_id_i = Input(UInt(4.W))
        val axi_b_user_i = Input(UInt(1.W))


        val axi_ar_ready_i = Input(UInt(1.W))
        val axi_ar_valid_o = Output(UInt(1.W))
        val axi_ar_addr_o = Output(UInt(32.W))
        val axi_ar_prot_o = Output(UInt(3.W))
        val axi_ar_id_o = Output(UInt(4.W))
        val axi_ar_user_o = Output(UInt(1.W))
        val axi_ar_len_o = Output(UInt(8.W))
        val axi_ar_size_o = Output(UInt(3.W))
        val axi_ar_burst_o = Output(UInt(2.W))
        val axi_ar_lock_o = Output(UInt(1.W))
        val axi_ar_cache_o = Output(UInt(4.W))
        val axi_ar_qos_o = Output(UInt(4.W))
        val axi_ar_region_o = Output(UInt(4.W))


        val axi_r_ready_o = Output(UInt(1.W))
        val axi_r_valid_i = Input(UInt(1.W))
        val axi_r_resp_i = Input(UInt(2.W))
        val axi_r_data_i = Input(UInt(64.W))
        val axi_r_last_i = Input(UInt(1.W))
        val axi_r_id_i = Input(UInt(4.W))
        val axi_r_user_i = Input(UInt(1.W))
    })
    setInline("axi_rw.v",
                """
|// include "defines.v"
|
|// Burst types
|`define AXI_BURST_TYPE_FIXED                                2'b00               //突发类型  FIFO
|`define AXI_BURST_TYPE_INCR                                 2'b01               //ram  
|`define AXI_BURST_TYPE_WRAP                                 2'b10
|// Access permissions
|`define AXI_PROT_UNPRIVILEGED_ACCESS                        3'b000
|`define AXI_PROT_PRIVILEGED_ACCESS                          3'b001
|`define AXI_PROT_SECURE_ACCESS                              3'b000
|`define AXI_PROT_NON_SECURE_ACCESS                          3'b010
|`define AXI_PROT_DATA_ACCESS                                3'b000
|`define AXI_PROT_INSTRUCTION_ACCESS                         3'b100
|// Memory types (AR)
|`define AXI_ARCACHE_DEVICE_NON_BUFFERABLE                   4'b0000
|`define AXI_ARCACHE_DEVICE_BUFFERABLE                       4'b0001
|`define AXI_ARCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE     4'b0010
|`define AXI_ARCACHE_NORMAL_NON_CACHEABLE_BUFFERABLE         4'b0011
|`define AXI_ARCACHE_WRITE_THROUGH_NO_ALLOCATE               4'b1010
|`define AXI_ARCACHE_WRITE_THROUGH_READ_ALLOCATE             4'b1110
|`define AXI_ARCACHE_WRITE_THROUGH_WRITE_ALLOCATE            4'b1010
|`define AXI_ARCACHE_WRITE_THROUGH_READ_AND_WRITE_ALLOCATE   4'b1110
|`define AXI_ARCACHE_WRITE_BACK_NO_ALLOCATE                  4'b1011
|`define AXI_ARCACHE_WRITE_BACK_READ_ALLOCATE                4'b1111
|`define AXI_ARCACHE_WRITE_BACK_WRITE_ALLOCATE               4'b1011
|`define AXI_ARCACHE_WRITE_BACK_READ_AND_WRITE_ALLOCATE      4'b1111
|// Memory types (AW)
|`define AXI_AWCACHE_DEVICE_NON_BUFFERABLE                   4'b0000
|`define AXI_AWCACHE_DEVICE_BUFFERABLE                       4'b0001
|`define AXI_AWCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE     4'b0010
|`define AXI_AWCACHE_NORMAL_NON_CACHEABLE_BUFFERABLE         4'b0011
|`define AXI_AWCACHE_WRITE_THROUGH_NO_ALLOCATE               4'b0110
|`define AXI_AWCACHE_WRITE_THROUGH_READ_ALLOCATE             4'b0110
|`define AXI_AWCACHE_WRITE_THROUGH_WRITE_ALLOCATE            4'b1110
|`define AXI_AWCACHE_WRITE_THROUGH_READ_AND_WRITE_ALLOCATE   4'b1110
|`define AXI_AWCACHE_WRITE_BACK_NO_ALLOCATE                  4'b0111
|`define AXI_AWCACHE_WRITE_BACK_READ_ALLOCATE                4'b0111
|`define AXI_AWCACHE_WRITE_BACK_WRITE_ALLOCATE               4'b1111
|`define AXI_AWCACHE_WRITE_BACK_READ_AND_WRITE_ALLOCATE      4'b1111
|
|`define AXI_SIZE_BYTES_1                                    3'b000                //突发宽度�?个数据的宽度
|`define AXI_SIZE_BYTES_2                                    3'b001
|`define AXI_SIZE_BYTES_4                                    3'b010
|`define AXI_SIZE_BYTES_8                                    3'b011
|`define AXI_SIZE_BYTES_16                                   3'b100
|`define AXI_SIZE_BYTES_32                                   3'b101
|`define AXI_SIZE_BYTES_64                                   3'b110
|`define AXI_SIZE_BYTES_128                                  3'b111
|
|
|module ysyx_040154_axi_rw # (
|    parameter RW_DATA_WIDTH     = 64,
|    parameter RW_ADDR_WIDTH     = 32,
|    parameter AXI_DATA_WIDTH    = 64,
|    parameter AXI_ADDR_WIDTH    = 32,
|    parameter AXI_ID_WIDTH      = 4,
|    parameter AXI_STRB_WIDTH    = AXI_DATA_WIDTH/8,
|    parameter AXI_USER_WIDTH    = 1
|)(
|    input                               clock,
|    input                               reset,
|
|	input                               rw_valid_i,         //IF&MEM输入信号
|    input                               enw_i,
|	output reg                          rw_ready_o,         //IF&MEM输入信号
|    output [RW_DATA_WIDTH-1:0]          data_read_o,        //IF&MEM输入信号
|    input  [RW_DATA_WIDTH-1:0]          rw_w_data_i,        //IF&MEM输入信号
|    input  [RW_ADDR_WIDTH-1:0]          rw_addr_i,          //IF&MEM输入信号
|    input  [7:0]                        rw_size_i,          //IF&MEM输入信号
|
|
|
|    // Advanced eXtensible Interface
|    input                               axi_aw_ready_i,              
|    output                              axi_aw_valid_o,
|    output [AXI_ADDR_WIDTH-1:0]         axi_aw_addr_o,
|    output [2:0]                        axi_aw_prot_o,
|    output [AXI_ID_WIDTH-1:0]           axi_aw_id_o,
|    output [AXI_USER_WIDTH-1:0]         axi_aw_user_o,
|    output [7:0]                        axi_aw_len_o,
|    output [2:0]                        axi_aw_size_o,
|    output [1:0]                        axi_aw_burst_o,
|    output                              axi_aw_lock_o,
|    output [3:0]                        axi_aw_cache_o,
|    output [3:0]                        axi_aw_qos_o,
|    output [3:0]                        axi_aw_region_o,
|
|    input                               axi_w_ready_i,                
|    output                              axi_w_valid_o,
|    output [AXI_DATA_WIDTH-1:0]         axi_w_data_o,
|    output [AXI_DATA_WIDTH/8-1:0]       axi_w_strb_o,
|    output                              axi_w_last_o,
|    output [AXI_USER_WIDTH-1:0]         axi_w_user_o,
|    
|    output                              axi_b_ready_o,                
|    input                               axi_b_valid_i,
|    input  [1:0]                        axi_b_resp_i,                 
|    input  [AXI_ID_WIDTH-1:0]           axi_b_id_i,
|    input  [AXI_USER_WIDTH-1:0]         axi_b_user_i,
|
|    input                               axi_ar_ready_i,                
|    output                              axi_ar_valid_o,
|    output [AXI_ADDR_WIDTH-1:0]         axi_ar_addr_o,
|    output [2:0]                        axi_ar_prot_o,
|    output [AXI_ID_WIDTH-1:0]           axi_ar_id_o,
|    output [AXI_USER_WIDTH-1:0]         axi_ar_user_o,
|    output [7:0]                        axi_ar_len_o,
|    output [2:0]                        axi_ar_size_o,
|    output [1:0]                        axi_ar_burst_o,
|    output                              axi_ar_lock_o,
|    output [3:0]                        axi_ar_cache_o,
|    output [3:0]                        axi_ar_qos_o,
|    output [3:0]                        axi_ar_region_o,
|    
|    output                              axi_r_ready_o,                 
|    input                               axi_r_valid_i,                
|    input  [1:0]                        axi_r_resp_i,
|    input  [AXI_DATA_WIDTH-1:0]         axi_r_data_i,
|    input                               axi_r_last_i,
|    input  [AXI_ID_WIDTH-1:0]           axi_r_id_i,
|    input  [AXI_USER_WIDTH-1:0]         axi_r_user_i
|);
|    
|    // ------------------State Machine------------------TODO
|    parameter IDLE          = 3'b000;
|    parameter STATE_RADDR   = 3'b001;
|    parameter STATE_RDATA   = 3'b010;
|    parameter STATE_WADDR   = 3'b011;
|    parameter STATE_WDATA   = 3'b100;
|    parameter STATE_WRESP   = 3'b101;
|    parameter STATE_DONE    = 3'b110;
|
|    reg [2:0]cur_state;
|    reg [2:0]nxt_state;
|
|    wire raddr_ok = axi_ar_ready_i & axi_ar_valid_o;
|    wire rdata_ok = axi_r_ready_o & axi_r_valid_i & axi_r_last_i;
|    wire waddr_ok = axi_aw_ready_i & axi_aw_valid_o;
|    wire wdata_ok = axi_w_ready_i & axi_w_valid_o & axi_w_last_o;
|    wire wresp_ok = axi_b_ready_o & axi_b_valid_i;
|
|    always @(posedge clock) 
|    begin
|        if(reset) cur_state <= IDLE;
|        else cur_state <= nxt_state;    
|    end
|    // 写通道状态切换
|    always@(*)
|    begin
|        case(cur_state)
|        IDLE:begin if(rw_valid_i & !enw_i) nxt_state = STATE_RADDR;
|                   else if(rw_valid_i & enw_i) nxt_state = STATE_WADDR;
|                   else nxt_state = cur_state; end
|        STATE_RADDR:begin if(raddr_ok) nxt_state = STATE_RDATA;
|                          else nxt_state = cur_state; end
|        STATE_RDATA:begin if(rdata_ok) nxt_state = STATE_DONE;
|                          else nxt_state = cur_state; end
|        STATE_WADDR:begin if(waddr_ok & wdata_ok)  nxt_state = STATE_WRESP;
|                          else if(waddr_ok & !wdata_ok) nxt_state = STATE_WDATA;
|                          else nxt_state = cur_state; end
|        STATE_WDATA:begin if(wdata_ok) nxt_state = STATE_WRESP;
|                          else nxt_state = cur_state; end
|        STATE_WRESP:begin if(wresp_ok) nxt_state = STATE_DONE; 
|                          else nxt_state = cur_state; end
|        STATE_DONE:begin nxt_state = IDLE; end
|        default: begin nxt_state = cur_state; end
|        endcase
|    end
|    // 读通道状态切换
|    assign data_read_o = axi_r_data_i;
|    
|
|    // ------------------Write Transaction------------------
|    // parameter AXI_SIZE      = $clog2(AXI_DATA_WIDTH / 8); //等于3
|    wire [AXI_ID_WIDTH-1:0] axi_id              = {AXI_ID_WIDTH{1'b0}};
|    wire [AXI_USER_WIDTH-1:0] axi_user          = {AXI_USER_WIDTH{1'b0}};
|    wire [2:0]AXI_SIZE  = (rw_addr_i <=32'h1000_0fff && rw_addr_i >=32'h1000_0000 )?3'b0:
|                          (rw_addr_i < 32'h8000_0000 )?3'b10:3'b11;
|    wire [7:0] axi_len      =  8'b0 ;
|    wire [2:0] axi_size     = AXI_SIZE[2:0]; // 以字节为单位,在这个设计中,如果是Lite则是64bits八字节, 查阅手册得知axi_size等于3时一次传输八个字jie
|    // 写地址通道  以下没有备注初始化信号的都可能是你需要产生和用到的
|    assign axi_aw_valid_o   = (cur_state == STATE_WADDR);                                                       // AXI_Lite
|    assign axi_aw_addr_o    = rw_addr_i;                                                                        // AXI_Lite
|    assign axi_aw_prot_o    = `AXI_PROT_UNPRIVILEGED_ACCESS | `AXI_PROT_SECURE_ACCESS | `AXI_PROT_DATA_ACCESS;  //初始化信号即可AXI_Lite
|    assign axi_aw_id_o      = axi_id;                                                                           //初始化信号即�?
|    assign axi_aw_user_o    = axi_user;                                                                         //初始化信号即�?
|    assign axi_aw_len_o     = axi_len;
|    assign axi_aw_size_o    = axi_size;
|    assign axi_aw_burst_o   = `AXI_BURST_TYPE_INCR;                                                             
|    assign axi_aw_lock_o    = 1'b0;                                                                             //初始化信号即�?
|    assign axi_aw_cache_o   = `AXI_AWCACHE_WRITE_BACK_READ_AND_WRITE_ALLOCATE;                                  //初始化信号即�?
|    assign axi_aw_qos_o     = 4'h0;                                                                             //初始化信号即�?
|    assign axi_aw_region_o  = 4'h0;                                                                             //初始化信号即�?
|
|    // 写数据通道
|    reg [AXI_DATA_WIDTH-1:0] axi_w_data;
|    always@(*)
|    begin
|        case(rw_addr_i[2:0])
|        3'b000: begin axi_w_data = rw_w_data_i; end
|        3'b001: begin axi_w_data = {rw_w_data_i[RW_DATA_WIDTH-1-8 :0], {8{1'b0}}}; end
|        3'b010: begin axi_w_data = {rw_w_data_i[RW_DATA_WIDTH-1-16:0], {16{1'b0}}}; end
|        3'b011: begin axi_w_data = {rw_w_data_i[RW_DATA_WIDTH-1-24:0], {24{1'b0}}}; end
|        3'b100: begin axi_w_data = {rw_w_data_i[RW_DATA_WIDTH-1-32:0], {32{1'b0}}}; end
|        3'b101: begin axi_w_data = {rw_w_data_i[RW_DATA_WIDTH-1-40:0], {40{1'b0}}}; end
|        3'b110: begin axi_w_data = {rw_w_data_i[RW_DATA_WIDTH-1-48:0], {48{1'b0}}}; end
|        3'b111: begin axi_w_data = {rw_w_data_i[RW_DATA_WIDTH-1-56:0], {56{1'b0}}}; end
|        default:begin axi_w_data = rw_w_data_i; end
|        endcase
|    end
|    assign axi_w_valid_o    = (cur_state == STATE_WDATA) || (cur_state == STATE_WADDR);                         // AXI_Lite
|    assign axi_w_data_o     = axi_w_data ;                                                                     // AXI_Lite
|    assign axi_w_strb_o     = rw_size_i;                                                                        // AXI_Lite
|    assign axi_w_last_o     = 1'b1; //标志突发传输的最后一次,在AXILite中始终为1
|    assign axi_w_user_o     = axi_user;                                                                         //初始化信号即�?
|
|
|    // 写应答通道
|    assign axi_b_ready_o    = (cur_state == STATE_WDATA) || (cur_state == STATE_WADDR) || (cur_state == STATE_WRESP);// AXI_Lite
|
|    // ------------------Read Transaction------------------
|
|    // Read address channel signals
|    assign axi_ar_valid_o   = (cur_state == STATE_RADDR);// AXI_Lite
|    assign axi_ar_addr_o    = rw_addr_i;                                                                        // AXI_Lite
|    assign axi_ar_prot_o    = `AXI_PROT_UNPRIVILEGED_ACCESS | `AXI_PROT_SECURE_ACCESS | `AXI_PROT_DATA_ACCESS;  //初始化信号即可AXI_Lite
|    assign axi_ar_id_o      = axi_id;                                                                           //初始化信号即�?                        
|    assign axi_ar_user_o    = axi_user;                                                                         //初始化信号即�?
|    assign axi_ar_len_o     = axi_len;                                                                          
|    assign axi_ar_size_o    = axi_size;
|    assign axi_ar_burst_o   = `AXI_BURST_TYPE_INCR;
|    assign axi_ar_lock_o    = 1'b0;                                                                             //初始化信号即�?
|    assign axi_ar_cache_o   = `AXI_ARCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE;                                 //初始化信号即�?
|    assign axi_ar_qos_o     = 4'h0;                                                                             //初始化信号即�?
|    assign axi_ar_region_o  = 4'h0;
|
|    // Read data channel signals
|    assign axi_r_ready_o    = (cur_state == STATE_RDATA);                                         // AXI_Lite
|
|    // rw_ready_o
|    // assign rw_ready_o = (cur_state == STATE);
|    always@(*)
|    begin
|        case(cur_state)
|        STATE_RDATA:
|        begin
|            rw_ready_o = rdata_ok;
|        end
|        STATE_WRESP:
|        begin
|            rw_ready_o = wresp_ok;
|        end
|        default: begin rw_ready_o = 'd0; end
|        endcase
|    end
|endmodule
                """.stripMargin)
}