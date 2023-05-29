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
    val IDLE = "b0000".U
    val COMPARE_TAG = "b0001".U
    val CACHE_READ = "b0010".U
    val ALLOCATE_L64 = "b0011".U
    val ALLOCATE_H64 = "b0100".U
    val WAIT = "b0101".U
    val uncached_flag = Wire(Bool())
    uncached_flag := (io.cpu_addr < "h80000000".U(64.W))
    val uncached_memdata = RegInit(0.U(64.W))

    // val ALLOCATE_03 = "b1000".U
    // val ALLOCATE_47 = "b1001".U
    // val ALLOCATE_8b = "b1010".U
    // val ALLOCATE_cf = "b1011".U
    // val WRITE_BACKL64 = "b011".U
    // val WRITE_BACKH64 = "b110".U

    val nxt_state = Wire(UInt(4.W))
    val cur_state = RegNext(nxt_state, "b0000".U(4.W))
    val wdata2cache_tmp = RegInit(0.U(64.W))

    // val wdata2cache_03 = RegInit(0.U(32.W))
    // val wdata2cache_47 = RegInit(0.U(32.W))
    // val wdata2cache_8b = RegInit(0.U(32.W))
    // 所有输出的默认值
    nxt_state := cur_state
    io.data2cpu := Mux(uncached_flag, uncached_memdata, 
                   Mux(io.cpu_addr(3), io.cache_data(127, 64),io.cache_data(63, 0))) // 总是读取地址为`raddr & ~0x7ull`的8字节返回给`rdata`
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
        when(!uncached_flag)
        {
            // cache命中并且valid为1
            when((io.cache_tag === io.cpu_addr(63, 12)) && (io.cache_valid === 1.U))
            {
                // nxt_state := CACHE_READ
                nxt_state := IDLE
                io.ready2cpu := true.B
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
            }
        }.elsewhen(uncached_flag)
        {
            io.valid2mem := true.B
            when(io.mem_ready)
            {
                nxt_state := CACHE_READ
                uncached_memdata := io.mem_data
            }
        }
    }.elsewhen(cur_state === CACHE_READ) // 单端口RAM不支持同时读写,所以需要加一个单独的READ状态
    {
        io.ready2cpu := true.B
        nxt_state := IDLE
    }.elsewhen(cur_state === ALLOCATE_L64)
    {
        io.valid2mem := true.B
        io.addr2mem := Cat(io.cpu_addr(63, 4), "h0".U(4.W))
        when(io.mem_ready)
        {
            wdata2cache_tmp := io.mem_data
            nxt_state := ALLOCATE_H64
        }.otherwise
        {
            nxt_state := ALLOCATE_L64
        }
    }.elsewhen(cur_state === ALLOCATE_H64)
    {
        io.valid2mem := true.B
        io.addr2mem := Cat(io.cpu_addr(63, 4), "h8".U(4.W))
        when(io.mem_ready)
        {
            io.enw2cache := true.B
            io.wdata2cache := Cat(io.mem_data, wdata2cache_tmp)
            nxt_state := WAIT
        }.otherwise
        {
            nxt_state := ALLOCATE_H64
        }
    }.elsewhen(cur_state === WAIT)
    {
        nxt_state := CACHE_READ
    }
}

// .elsewhen(cur_state === ALLOCATE_03)
//     {
//         io.valid2mem := true.B
//         io.addr2mem := Cat(io.cpu_addr(63, 4), "h0".U(4.W))
//         when(io.mem_ready)
//         {
//             wdata2cache_03 := io.mem_data(31, 0)
//             nxt_state := ALLOCATE_47
//         }.otherwise
//         {
//             nxt_state := ALLOCATE_03
//         }
//     }.elsewhen(cur_state === ALLOCATE_47)
//     {
//         io.valid2mem := true.B
//         io.addr2mem := Cat(io.cpu_addr(63, 4), "h4".U(4.W))
//         when(io.mem_ready)
//         {
//             wdata2cache_47 := io.mem_data(63, 32)
//             nxt_state := ALLOCATE_8b
//         }.otherwise
//         {
//             nxt_state := ALLOCATE_47
//         }
//     }.elsewhen(cur_state === ALLOCATE_8b)
//     {
//         io.valid2mem := true.B
//         io.addr2mem := Cat(io.cpu_addr(63, 4), "h8".U(4.W))
//         when(io.mem_ready)
//         {
//             wdata2cache_8b := io.mem_data(31, 0)
//             nxt_state := ALLOCATE_cf
//         }.otherwise
//         {
//             nxt_state := ALLOCATE_8b
//         }
//     }.elsewhen(cur_state === ALLOCATE_cf)
//     {
//         io.valid2mem := true.B
//         io.addr2mem := Cat(io.cpu_addr(63, 4), "hc".U(4.W))
//         when(io.mem_ready)
//         {
//             io.enw2cache := true.B
//             io.wdata2cache := Cat(io.mem_data(63, 32), wdata2cache_8b, wdata2cache_47, wdata2cache_03)
//             nxt_state := COMPARE_TAG
//         }.otherwise
//         {
//             nxt_state := ALLOCATE_cf
//         }
    // }