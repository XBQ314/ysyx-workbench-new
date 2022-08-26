import chisel3._
import chisel3.util._
import xbqpackage._

class ysyx_22040154_axi_rw extends BlackBox with HasBlackBoxInline
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
|module ysyx_22040154_axi_rw # (
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
|	 input                               rw_valid_i,         //IF&MEM输入信号
|    input                               enw_i,
|	 output                              rw_ready_o,         //IF&MEM输入信号
|    output     [RW_DATA_WIDTH-1:0]      data_read_o,        //IF&MEM输入信号
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
|    wire rstn;
|    assign rstn = !reset;
|    // ------------------State Machine------------------TODO
|    parameter IDLE      = 3'b000;
|    parameter ARVALID   = 3'b001;
|    parameter RREADY    = 3'b010;
|    parameter AWVALID   = 3'b011;
|    parameter WVALID    = 3'b100;
|    parameter BREADY    = 3'b101;
|
|    reg [2:0] cur_state;
|    reg [2:0]nxt_state;
|
|    always @(posedge clock) 
|    begin
|        if(!rstn) cur_state <= IDLE;
|        else cur_state <= nxt_state;    
|    end
|    // 写通道状态切换
|    always@(*)
|    begin
|        case(cur_state)
|        IDLE:begin if(rw_valid_i & !enw_i) nxt_state = ARVALID;
|                   else if(rw_valid_i & enw_i) nxt_state = AWVALID;
|                   else nxt_state = cur_state;end
|        ARVALID:begin if(axi_ar_ready_i) nxt_state = RREADY;
|                      else nxt_state = cur_state;end
|        RREADY:begin if(axi_r_valid_i) nxt_state = IDLE;
|                     else nxt_state = cur_state;end
|        AWVALID:begin if(axi_aw_ready_i) nxt_state = WVALID;
|                      else nxt_state = cur_state;end
|        WVALID:begin if(axi_w_ready_i) nxt_state = BREADY;
|                     else nxt_state = cur_state;end
|        BREADY:begin if(axi_b_valid_i) nxt_state = IDLE;
|                     else nxt_state = cur_state;end
|        default: begin nxt_state = cur_state; end
|        endcase
|    end
|    // 读通道状态切换
|    assign data_read_o = axi_r_data_i;
|    
|
|    // ------------------Write Transaction------------------
|    parameter AXI_SIZE      = $clog2(AXI_DATA_WIDTH / 8); //等于3
|    wire [AXI_ID_WIDTH-1:0] axi_id              = {AXI_ID_WIDTH{1'b0}};
|    wire [AXI_USER_WIDTH-1:0] axi_user          = {AXI_USER_WIDTH{1'b0}};
|    wire [7:0] axi_len      =  8'b0 ;
|    wire [2:0] axi_size     = AXI_SIZE[2:0]; // 以字节为单位,在这个设计中，如果是Lite则是64bits八字节, 查阅手册得知axi_size等于3时一次传输八个字�?
|    // 写地址通道  以下没有备注初始化信号的都可能是你需要产生和用到的
|    assign axi_aw_valid_o   = (cur_state == AWVALID);                                                           // AXI_Lite
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
|    assign axi_w_valid_o    = (cur_state == WVALID);                                                            // AXI_Lite
|    assign axi_w_data_o     = rw_w_data_i ;                                                                     // AXI_Lite
|    assign axi_w_strb_o     = rw_size_i;                                                                        // AXI_Lite
|    assign axi_w_last_o     = 1'b0;
|    assign axi_w_user_o     = axi_user;                                                                         //初始化信号即�?
|
|    // 写应答通道
|    assign axi_b_ready_o    = (cur_state == BREADY);                                                            // AXI_Lite
|
|    // ------------------Read Transaction------------------
|
|    // Read address channel signals
|    assign axi_ar_valid_o   = (cur_state == ARVALID);                                                           // AXI_Lite
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
|    // always@(*)
|    // begin
|    //     case(cur_state)
|    //     // IDLE:
|    //     // begin
|    //     // end
|    //     ARVALID:
|    //     begin
|    //     end
|    //     RREADY:
|    //     begin
|    //     end
|    //     // AWVALID:
|    //     // begin
|    //     // end
|    //     // WVALID:
|    //     // begin
|    //     // end
|    //     // BREADY:
|    //     // begin
|    //     // end
|    //     default: begin end
|    //     endcase
|    // end
|
|    // Read data channel signals
|    assign axi_r_ready_o    = (cur_state == RREADY) && (axi_r_valid_i);                                         // AXI_Lite
|
|    // rw_ready_o
|    assign rw_ready_o = (cur_state == IDLE);
|endmodule
|
                """.stripMargin)
}