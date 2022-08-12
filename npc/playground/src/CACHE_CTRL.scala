import chisel3._
import chisel3.util._

class CACHE_CTRL extends Module
{
    val io = IO(new Bundle
    {
        // 与CPU交互
        val cpu_addr = Input(UInt(64.W))
        val cpu_data = Input(UInt(64.W)) // used when write
        val cpu_enw = Input(Bool()) // used when write
        val cpu_wmask = Input(UInt(8.W)) // used when write
        val cpu_valid = Input(Bool())

        val data2cpu = Output(UInt(64.W))
        val ready2cpu = Output(Bool()) // result is ready

        // 与MEM交互
        val addr2mem = Output(UInt(64.W))
        val data2mem = Output(UInt(64.W)) // used when write
        val wmask2mem = Output(UInt(8.W)) // used when write
        val valid2mem = Output(Bool())
        val enw2mem = Output(Bool()) // used when write

        val mem_data = Input(UInt(64.W))
        val mem_ready = Input(Bool())

        // 与Cache交互
        val index2cache = Output(UInt(8.W))
        val enw2cache = Output(Bool())
        val tagenw2cache = Output(Bool())
        val wdata2cache = Output(UInt(128.W)) // used when write
        val valid2cache = Output(UInt(1.W))
        val dirty2cache = Output(UInt(1.W))
        val tag2cache = Output(UInt(52.W))

        val cache_data = Input(UInt(128.W))
        val cache_valid = Input(UInt(1.W))
        val cache_dirty = Input(UInt(1.W))
        val cache_tag = Input(UInt(52.W))
    })
    val IDLE = "b000".U
    val COMPARE_TAG = "b001".U
    val CACHE_READ = "b100".U
    val ALLOCATE_L64 = "b010".U
    val ALLOCATE_H64 = "b101".U
    // val WRITE_BACKL64 = "b011".U
    // val WRITE_BACKH64 = "b110".U

    val nxt_state = Wire(UInt(3.W))
    val cur_state = RegNext(nxt_state, "b000".U)
    val wdata2cache_L64 = RegInit(0.U)
    // 所有输出的默认值
    nxt_state := cur_state
    io.data2cpu := Mux(io.cpu_addr(3), io.cache_data(127, 64),io.cache_data(63, 0)) // 总是读取地址为`raddr & ~0x7ull`的8字节返回给`rdata`
    io.ready2cpu := false.B

    io.addr2mem := io.cpu_addr
    io.data2mem := io.cpu_data
    io.wmask2mem := io.cpu_wmask
    io.valid2mem := false.B
    io.enw2mem := false.B

    io.index2cache := io.cpu_addr(11, 4)
    io.enw2cache := false.B
    io.tagenw2cache := false.B
    io.wdata2cache := io.cache_data
    // switch(Cat(io.cpu_addr(3), io.cpu_wmask))
    // {
    //     is("b0_0000_0001".U){io.wdata2cache(7, 0) := io.cpu_data(7, 0)}
    //     is("b0_0000_0011".U){io.wdata2cache(15, 0) := io.cpu_data(15, 0)}
    //     is("b0_0000_1111".U){io.wdata2cache(31, 0) := io.cpu_data(31, 0)}
    //     is("b0_1111_1111".U){io.wdata2cache(63, 0) := io.cpu_data(63, 0)}
    //     is("b1_0000_0001".U){io.wdata2cache(71, 64) := io.cpu_data(7, 0)}
    //     is("b1_0000_0011".U){io.wdata2cache(79, 64) := io.cpu_data(15, 0)}
    //     is("b1_0000_1111".U){io.wdata2cache(95, 64) := io.cpu_data(31, 0)}
    //     is("b1_1111_1111".U){io.wdata2cache(127, 64) := io.cpu_data(63, 0)}
    // }
    io.valid2cache := 0.U
    io.dirty2cache := 0.U
    io.tag2cache := 0.U

    // FSM
    when(cur_state === IDLE)
    {
        when(io.cpu_valid)
        {
            nxt_state := COMPARE_TAG
        }.otherwise
        {
            nxt_state := IDLE
        }
    }.elsewhen(cur_state === COMPARE_TAG)
    {
        // cache命中并且valid为1
        when((io.cache_tag === io.cpu_addr(63, 12)) && (io.cache_valid === 1.U))
        {
            // when(io.cpu_enw) // 写命中,只更新cache中的数据,之后再写回mem
            // {
            //     io.enw2cache := true.B
            //     io.tagenw2cache := true.B

            //     io.tag2cache := io.cache_tag
            //     io.valid2cache := 1.U
            //     io.dirty2cache := 1.U
            // }
            nxt_state := CACHE_READ
        }.otherwise // miss
        {
            io.tagenw2cache := true.B
            io.valid2cache := 1.U
            io.tag2cache := io.cpu_addr(63, 12)
            io.dirty2cache := io.cpu_enw

            // 因为miss,所以进行访存
            io.valid2mem := true.B
            // 块无效或者未被重写过,所以不需要写回mem中,直接进入分配状态
            when(io.cache_valid === 0.U || io.cache_dirty === 0.U)
            {
                nxt_state := ALLOCATE_L64
            }
            // .otherwise // 需要写回mem
            // {
            //     // 111111111111111111111111111111111111111
            //     io.enw2mem := true.B
            //     nxt_state := WRITE_BACKL64
            // }
        }
    }.elsewhen(cur_state === CACHE_READ) // 单端口RAM不支持同时读写,所以需要加一个单独的READ状态
    {
        io.ready2cpu := true.B
        nxt_state := IDLE
    }.elsewhen(cur_state === ALLOCATE_L64)
    {
        io.addr2mem := Cat(io.cpu_addr(63, 4), "b0000".U(4.W))
        when(io.mem_ready)
        {
            io.enw2cache := true.B
            wdata2cache_L64 := io.mem_data
            nxt_state := ALLOCATE_H64
        }.otherwise
        {
            nxt_state := ALLOCATE_L64
        }
    }.elsewhen(cur_state === ALLOCATE_H64)
    {
        io.addr2mem := Cat(io.cpu_addr(63, 4), "b1000".U(4.W))
        when(io.mem_ready)
        {
            io.enw2cache := true.B
            io.wdata2cache := Cat(io.mem_data, wdata2cache_L64)
            nxt_state := COMPARE_TAG
        }.otherwise
        {
            nxt_state := ALLOCATE_H64
        }
    }
    // .elsewhen(cur_state === WRITE_BACKL64)
    // {
    //     when(io.mem_ready)
    //     {
    //         // 111111111111111111111111111111111111111
    //         io.enw2mem := false.B
    //         io.valid2mem := false.B
    //         nxt_state := WRITE_BACKH64
    //     }.otherwise
    //     {
    //         nxt_state := WRITE_BACKL64
    //     }
    // }.elsewhen(cur_state === WRITE_BACKH64)
    // {
    //     when(io.mem_ready)
    //     {
    //         // 111111111111111111111111111111111111111
    //         io.enw2mem := false.B
    //         io.valid2mem := false.B
    //         nxt_state := ALLOCATE_L64
    //     }.otherwise
    //     {
    //         nxt_state := WRITE_BACKH64
    //     }
    // }
}