import chisel3._
import chisel3.util._

class DCACHE_CTRL extends Module
{
    val io = IO(new Bundle
    {
        // 与CPU交互
        val cpu_addr = Input(UInt(64.W))
        val cpu_data = Input(UInt(64.W)) // used when write
        val cpu_enw = Input(Bool()) // used when write
        val cpu_wmask = Input(UInt(8.W)) // used when write
        val cpu_valid = Input(Bool())
        val fencei_flag = Input(Bool())

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
        val wmask2cache = Output(UInt(128.W)) // used when write
        val valid2cache = Output(UInt(1.W))
        val dirty2cache = Output(UInt(1.W))
        val tag2cache = Output(UInt(52.W))

        val cache_data = Input(UInt(128.W))
        val cache_valid = Input(UInt(1.W))
        val cache_dirty = Input(UInt(1.W))
        val cache_tag = Input(UInt(52.W))

        val dcache_stall_req = Output(Bool())
    })
    val IDLE = "b0000".U
    val COMPARE_TAG = "b0001".U
    val CACHE_READ = "b0010".U
    val ALLOCATE_L64 = "b0011".U
    val ALLOCATE_H64 = "b0100".U
    val WRITE_BACKL64 = "b0101".U
    val WRITE_BACKH64 = "b0110".U
    val ALLOCATE_03 = "b1000".U
    val ALLOCATE_47 = "b1001".U
    val ALLOCATE_8b = "b1010".U
    val ALLOCATE_cf = "b1011".U
    val CLEAN = "b1100".U
    val CLEAN_WBL64 = "b1101".U
    val CLEAN_WBH64 = "b1110".U


    val nxt_state = Wire(UInt(4.W))
    val cur_state = RegNext(nxt_state, "b0000".U)
    val wdata2cache_L64 = RegInit(0.U)
    val uncached_flag   = Wire(Bool())
    val clint_flag      = Wire(Bool())
    val uart_flag       = Wire(Bool())
    val spi_flag        = Wire(Bool())
    val vga_flag        = Wire(Bool())
    val sp2_flag        = Wire(Bool())
    val ethernet_flag   = Wire(Bool())
    val flashXIP_flag   = Wire(Bool())
    val chiplink_flag   = Wire(Bool())
    val mem_flag        = Wire(Bool())
    val sdram_flag      = Wire(Bool())
    val CLEANidx = RegInit(0.U(8.W))
    CLEANidx := CLEANidx

    uncached_flag := ((io.cpu_addr === "ha0000048".U(64.W) || io.cpu_addr === "ha00003f8".U(64.W)))
    clint_flag      := ((io.cpu_addr >= "h02000000".U(64.W) && io.cpu_addr <= "h0200Bfff".U(64.W)))
    uart_flag       := ((io.cpu_addr >= "h10000000".U(64.W) && io.cpu_addr <= "h10000fff".U(64.W)))
    spi_flag        := ((io.cpu_addr >= "h10001000".U(64.W) && io.cpu_addr <= "h10001fff".U(64.W)))
    vga_flag        := ((io.cpu_addr >= "h10002000".U(64.W) && io.cpu_addr <= "h10002fff".U(64.W)))
    sp2_flag        := ((io.cpu_addr >= "h10003000".U(64.W) && io.cpu_addr <= "h10003fff".U(64.W)))
    ethernet_flag   := ((io.cpu_addr >= "h10004000".U(64.W) && io.cpu_addr <= "h10004fff".U(64.W)))
    flashXIP_flag   := ((io.cpu_addr >= "h30000000".U(64.W) && io.cpu_addr <= "h3fffffff".U(64.W)))
    chiplink_flag   := ((io.cpu_addr >= "h40000000".U(64.W) && io.cpu_addr <= "h7fffffff".U(64.W)))
    mem_flag        := ((io.cpu_addr >= "h80000000".U(64.W) && io.cpu_addr <= "hfbffffff".U(64.W)))
    sdram_flag      := ((io.cpu_addr >= "hfc000000".U(64.W) && io.cpu_addr <= "hffffffff".U(64.W)))

    // uncached_flag := !(mem_flag || flashXIP_flag)
    val uncached_memdata = RegInit(0.U(64.W))
    val wdata2cache_03 = RegInit(0.U(32.W))
    val wdata2cache_47 = RegInit(0.U(32.W))
    val wdata2cache_8b = RegInit(0.U(32.W))
    // 所有输出的默认值
    nxt_state := cur_state
    io.data2cpu := Mux(uncached_flag, uncached_memdata, 
                   Mux(io.cpu_addr(3), io.cache_data(127, 64),io.cache_data(63, 0))) // 总是读取地址为`raddr & ~0x7ull`的8字节返回给`rdata`
    io.ready2cpu := false.B

    io.addr2mem := io.cpu_addr
    io.data2mem := io.cache_data(63, 0)
    io.wmask2mem := Mux(io.enw2mem, "hff".U(8.W), 0.U(8.W)) // 因为写回mem时肯定是data全部更新
    io.valid2mem := false.B
    io.enw2mem := false.B

    val shift = Wire(UInt(6.W))
    val oldtag = RegInit(0.U(52.W))
    oldtag := oldtag
    shift := Cat(io.cpu_addr(2, 0), "b000".U(3.W))
    io.index2cache := io.cpu_addr(11, 4)
    io.enw2cache := false.B
    io.tagenw2cache := false.B
    io.wdata2cache := Mux(io.cpu_addr(3), Cat(io.cpu_data << shift, 0.U(64.W)), Cat(0.U(64.W), io.cpu_data << shift))
    // io.wdata2cache := Mux(io.cpu_addr(3), Cat((io.cpu_data << io.cpu_addr(2, 0)), 0.U(64.W)), Cat(0.U(64.W), (io.cpu_data << io.cpu_addr(2, 0))))
    io.wmask2cache := 0.U
    switch(Cat(io.cpu_addr(3), io.cpu_wmask))
    {
        is("b0_0000_0001".U){io.wmask2cache := "h0000_0000_0000_0000_0000_0000_0000_00ff".U(128.W) << shift}
        is("b0_0000_0011".U){io.wmask2cache := "h0000_0000_0000_0000_0000_0000_0000_ffff".U(128.W) << shift}
        is("b0_0000_1111".U){io.wmask2cache := "h0000_0000_0000_0000_0000_0000_ffff_ffff".U(128.W) << shift}
        is("b0_1111_1111".U){io.wmask2cache := "h0000_0000_0000_0000_ffff_ffff_ffff_ffff".U(128.W) << shift}
        is("b1_0000_0001".U){io.wmask2cache := "h0000_0000_0000_00ff_0000_0000_0000_0000".U(128.W) << shift}
        is("b1_0000_0011".U){io.wmask2cache := "h0000_0000_0000_ffff_0000_0000_0000_0000".U(128.W) << shift}
        is("b1_0000_1111".U){io.wmask2cache := "h0000_0000_ffff_ffff_0000_0000_0000_0000".U(128.W) << shift}
        is("b1_1111_1111".U){io.wmask2cache := "hffff_ffff_ffff_ffff_0000_0000_0000_0000".U(128.W) << shift}
        // is("b0_0000_0001".U){io.wmask2cache := "h0000_0000_0000_0000_0000_0000_0000_000f".U(128.W)}
        // is("b0_0000_0011".U){io.wmask2cache := "h0000_0000_0000_0000_0000_0000_0000_ffff".U(128.W)}
        // is("b0_0000_1111".U){io.wmask2cache := "h0000_0000_0000_0000_0000_0000_ffff_ffff".U(128.W)}
        // is("b0_1111_1111".U){io.wmask2cache := "h0000_0000_0000_0000_ffff_ffff_ffff_ffff".U(128.W)}
        // is("b1_0000_0001".U){io.wmask2cache := "h0000_0000_0000_000f_0000_0000_0000_0000".U(128.W)}
        // is("b1_0000_0011".U){io.wmask2cache := "h0000_0000_0000_ffff_0000_0000_0000_0000".U(128.W)}
        // is("b1_0000_1111".U){io.wmask2cache := "h0000_0000_ffff_ffff_0000_0000_0000_0000".U(128.W)}
        // is("b1_1111_1111".U){io.wmask2cache := "hffff_ffff_ffff_ffff_0000_0000_0000_0000".U(128.W)}
    }
    io.valid2cache := 0.U
    io.dirty2cache := 0.U
    io.tag2cache := 0.U

    io.dcache_stall_req := true.B
    // FSM
    when(cur_state === IDLE)
    {
        io.ready2cpu := true.B
        io.dcache_stall_req := false.B
        when(io.cpu_valid && ~clint_flag) // 因为CLINT是core内部实现的外设, 所以不需要发出访存信号(无论是cache还是mem都不需要)
        {
            nxt_state := Mux(io.fencei_flag, CLEAN, COMPARE_TAG)

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
                when(io.cpu_enw) // 写命中,只更新cache中的数据,之后再写回mem
                {
                    io.enw2cache := true.B
                    io.tagenw2cache := true.B

                    io.tag2cache := io.cache_tag
                    io.valid2cache := 1.U
                    io.dirty2cache := 1.U
                }
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
                    when(flashXIP_flag)
                    {
                        nxt_state := ALLOCATE_03
                    }.otherwise
                    {
                        nxt_state := ALLOCATE_L64
                    }
                }.otherwise // 需要写回mem
                {
                    // 111111111111111111111111111111111111111
                    io.enw2mem := true.B
                    oldtag := io.cache_tag
                    nxt_state := WRITE_BACKL64
                }
            }
        }.elsewhen(uncached_flag)
        {
            io.addr2mem := io.cpu_addr
            io.data2mem := io.cpu_data
            io.wmask2mem := io.cpu_wmask
            // io.valid2mem := false.B // 应该是为了禁止访存
            io.valid2mem := true.B
            io.enw2mem := (io.cpu_wmask =/= 0.U)
            io.dcache_stall_req := false.B
            when(io.mem_ready)
            {
                nxt_state := CACHE_READ
                uncached_memdata := io.mem_data
            }
        }
    }.elsewhen(cur_state === CACHE_READ) // 单端口RAM不支持同时读写,所以需要加一个单独的READ状态
    {
        // io.dcache_stall_req := false.B
        io.ready2cpu := true.B
        nxt_state := IDLE
    }.elsewhen(cur_state === ALLOCATE_L64)
    {
        io.addr2mem := Cat(io.cpu_addr(63, 4), "b0000".U(4.W))
        io.valid2mem := true.B
        when(io.mem_ready)
        {
            // io.enw2cache := true.B
            wdata2cache_L64 := io.mem_data
            nxt_state := ALLOCATE_H64
        }.otherwise
        {
            nxt_state := ALLOCATE_L64
        }
    }.elsewhen(cur_state === ALLOCATE_H64)
    {
        io.addr2mem := Cat(io.cpu_addr(63, 4), "b1000".U(4.W))
        io.valid2mem := true.B
        when(io.mem_ready)
        {
            io.enw2cache := true.B // 对cache真正的分配写入发生在ALLOCATE_H64
            io.wdata2cache := Cat(io.mem_data, wdata2cache_L64)
            io.wmask2cache := "hffff_ffff_ffff_ffff_ffff_ffff_ffff_ffff".U(128.W)
            nxt_state := COMPARE_TAG
        }.otherwise
        {
            nxt_state := ALLOCATE_H64
        }
    }.elsewhen(cur_state === ALLOCATE_03)
    {
        io.addr2mem := Cat(io.cpu_addr(63, 4), "h0".U(4.W))
        io.valid2mem := true.B
        when(io.mem_ready)
        {
            wdata2cache_03 := io.mem_data(31, 0)
            nxt_state := ALLOCATE_47
        }.otherwise
        {
            nxt_state := ALLOCATE_03
        }
    }.elsewhen(cur_state === ALLOCATE_47)
    {
        io.addr2mem := Cat(io.cpu_addr(63, 4), "h4".U(4.W))
        io.valid2mem := true.B
        when(io.mem_ready)
        {
            wdata2cache_47 := io.mem_data(31, 0)
            nxt_state := ALLOCATE_8b
        }.otherwise
        {
            nxt_state := ALLOCATE_47
        }
    }.elsewhen(cur_state === ALLOCATE_8b)
    {
        io.addr2mem := Cat(io.cpu_addr(63, 4), "h8".U(4.W))
        io.valid2mem := true.B
        when(io.mem_ready)
        {
            wdata2cache_8b := io.mem_data(31, 0)
            nxt_state := ALLOCATE_cf
        }.otherwise
        {
            nxt_state := ALLOCATE_8b
        }
    }.elsewhen(cur_state === ALLOCATE_cf)
    {
        io.addr2mem := Cat(io.cpu_addr(63, 4), "hc".U(4.W))
        io.valid2mem := true.B
        when(io.mem_ready)
        {
            io.enw2cache := true.B // 对cache真正的分配写入发生在ALLOCATE_H64
            io.wdata2cache := Cat(io.mem_data(31, 0), wdata2cache_8b, wdata2cache_47, wdata2cache_03)
            io.wmask2cache := "hffff_ffff_ffff_ffff_ffff_ffff_ffff_ffff".U(128.W)
            nxt_state := COMPARE_TAG
        }.otherwise
        {
            nxt_state := ALLOCATE_cf
        }
    }
    .elsewhen(cur_state === WRITE_BACKL64)
    {
        io.addr2mem := Cat(oldtag, io.index2cache, "b0000".U(4.W))
        io.data2mem := io.cache_data(63, 0)
        io.enw2mem := true.B
        io.valid2mem := true.B
        // io.valid2mem := false.B
        when(io.mem_ready)
        {
            // 111111111111111111111111111111111111111
            // io.enw2mem := true.B
            // io.valid2mem := true.B
            nxt_state := WRITE_BACKH64
        }.otherwise
        {
            nxt_state := WRITE_BACKL64
        }
    }.elsewhen(cur_state === WRITE_BACKH64)
    {
        io.addr2mem := Cat(oldtag, io.index2cache, "b1000".U(4.W))
        io.data2mem := io.cache_data(127, 64)
        io.enw2mem := true.B
        io.valid2mem := true.B
        // io.valid2mem := false.B
        when(io.mem_ready)
        {
            // 111111111111111111111111111111111111111
            io.enw2mem := false.B
            // io.valid2mem := false.B
            when(flashXIP_flag)
            {
                nxt_state := ALLOCATE_03
            }.otherwise
            {
                nxt_state := ALLOCATE_L64
            }
        }.otherwise
        {
            nxt_state := WRITE_BACKH64
        }
    }.elsewhen(cur_state === CLEAN)
    {
        io.index2cache := CLEANidx
        when((io.cache_valid === 1.U) && (io.cache_dirty === 1.U))
        {
            nxt_state := CLEAN_WBL64
        }.otherwise
        {
            CLEANidx := CLEANidx +1.U
            nxt_state := Mux(CLEANidx === 255.U, IDLE, CLEAN)
        }
    }.elsewhen(cur_state === CLEAN_WBL64)
    {
        io.index2cache := CLEANidx
        io.addr2mem := Cat(io.cache_tag, io.index2cache, "b0000".U(4.W))
        io.data2mem := io.cache_data(63, 0)
        io.enw2mem := true.B
        io.valid2mem := true.B
        when(io.mem_ready)
        {
            nxt_state := CLEAN_WBH64
        }.otherwise
        {
            nxt_state := CLEAN_WBL64
        }
    }.elsewhen(cur_state === CLEAN_WBH64)
    {
        io.index2cache := CLEANidx
        io.addr2mem := Cat(io.cache_tag, io.index2cache, "b1000".U(4.W))
        io.data2mem := io.cache_data(127, 64)
        io.enw2mem := true.B
        io.valid2mem := true.B
        when(io.mem_ready)
        {
            io.enw2mem := false.B
            CLEANidx := CLEANidx +1.U
            when(CLEANidx === 255.U)
            {
                nxt_state := IDLE
            }.otherwise
            {
                nxt_state := CLEAN
            }
        }.otherwise
        {
            nxt_state := CLEAN_WBH64
        }
    }
    

    // .elsewhen(cur_state === UNCACHED) // 如果是触发了UNCACHE的外设访存，直接将CPU信号越过CACHE进行输出
    // {
    //     io.addr2mem := io.cpu_addr
    //     io.data2mem := io.cpu_data
    //     io.wmask2mem := io.cpu_wmask
    //     io.valid2mem := false.B // 应该是为了禁止访存
    //     // io.valid2mem := true.B
    //     io.enw2mem := false.B
    //     io.dcache_stall_req := false.B
    //     // when(io.mem_ready)
    //     // {
    //         nxt_state := IDLE
    //     // }.otherwise
    //     // {
    //     //     nxt_state := UNCACHED
    //     // }
    // }
    // .elsewhen(cur_state === UNCACHED) // 如果是触发了UNCACHE的外设访存，直接将CPU信号越过CACHE进行输出
    // {
    //     io.addr2mem := io.cpu_addr
    //     io.data2mem := io.cpu_data
    //     io.wmask2mem := io.cpu_wmask
    //     // io.valid2mem := false.B // 应该是为了禁止访存
    //     io.valid2mem := true.B
    //     io.enw2mem := (io.cpu_wmask =/= 0.U)
    //     when(io.mem_ready)
    //     {
    //         nxt_state := IDLE
    //     }.otherwise
    //     {
    //         nxt_state := UNCACHED
    //     }
    // }
}