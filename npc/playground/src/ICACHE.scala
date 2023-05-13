import chisel3._
import chisel3.util._

class ICACHE extends Module
{
    val io = IO(new Bundle
    {
        val CLK = Input(Clock())

        val index = Input(UInt(8.W))
        val enw = Input(Bool())
        val tag_enw = Input(Bool())
        val wdata = Input(UInt(128.W))
        val in_valid = Input(UInt(1.W))
        val in_dirty = Input(UInt(1.W))
        val in_tag = Input(UInt(52.W))

        val data = Output(UInt(128.W))
        val valid = Output(UInt(1.W))
        val dirty = Output(UInt(1.W))
        val tag = Output(UInt(52.W))

        // // TOP SRAM IO
        // val sram0_addr      = Output(UInt(6.W))
        // val sram0_cen       = Output(UInt(1.W))
        // val sram0_wen       = Output(UInt(1.W))
        // val sram0_wmask     = Output(UInt(128.W))
        // val sram0_wdata     = Output(UInt(128.W))
        // val sram0_rdata     = Input(UInt(128.W))

        // val sram1_addr      = Output(UInt(6.W))
        // val sram1_cen       = Output(UInt(1.W))
        // val sram1_wen       = Output(UInt(1.W))
        // val sram1_wmask     = Output(UInt(128.W))
        // val sram1_wdata     = Output(UInt(128.W))
        // val sram1_rdata     = Input(UInt(128.W))

        // val sram2_addr      = Output(UInt(6.W))
        // val sram2_cen       = Output(UInt(1.W))
        // val sram2_wen       = Output(UInt(1.W))
        // val sram2_wmask     = Output(UInt(128.W))
        // val sram2_wdata     = Output(UInt(128.W))
        // val sram2_rdata     = Input(UInt(128.W))

        // val sram3_addr      = Output(UInt(6.W))
        // val sram3_cen       = Output(UInt(1.W))
        // val sram3_wen       = Output(UInt(1.W))
        // val sram3_wmask     = Output(UInt(128.W))
        // val sram3_wdata     = Output(UInt(128.W))
        // val sram3_rdata     = Input(UInt(128.W))
    })
    val RAM0 = Module(new S011HD1P_X32Y2D128())
    val RAM1 = Module(new S011HD1P_X32Y2D128())
    val RAM2 = Module(new S011HD1P_X32Y2D128())
    val RAM3 = Module(new S011HD1P_X32Y2D128())

    // 默认值
    RAM0.io.CLK := io.CLK
    RAM0.io.CEN := 1.U
    RAM0.io.WEN := !io.enw
    RAM0.io.A   := io.index(5, 0)
    RAM0.io.D   := io.wdata
    // io.sram0_addr   := io.index(5, 0)
    // io.sram0_cen    := 1.U
    // io.sram0_wen    := !io.enw
    // io.sram0_wmask  := 0.U
    // io.sram0_wdata  := io.wdata


    RAM1.io.CLK := io.CLK
    RAM1.io.CEN := 1.U
    RAM1.io.WEN := !io.enw
    RAM1.io.A   := io.index(5, 0)
    RAM1.io.D   := io.wdata
    // io.sram1_addr   := io.index(5, 0)
    // io.sram1_cen    := 1.U
    // io.sram1_wen    := !io.enw
    // io.sram1_wmask  := 0.U
    // io.sram1_wdata  := io.wdata

    RAM2.io.CLK := io.CLK
    RAM2.io.CEN := 1.U
    RAM2.io.WEN := !io.enw
    RAM2.io.A   := io.index(5, 0)
    RAM2.io.D   := io.wdata
    // io.sram2_addr   := io.index(5, 0)
    // io.sram2_cen    := 1.U
    // io.sram2_wen    := !io.enw
    // io.sram2_wmask  := 0.U
    // io.sram2_wdata  := io.wdata

    RAM3.io.CLK := io.CLK
    RAM3.io.CEN := 1.U
    RAM3.io.WEN := !io.enw
    RAM3.io.A   := io.index(5, 0)
    RAM3.io.D   := io.wdata
    // io.sram3_addr   := io.index(5, 0)
    // io.sram3_cen    := 1.U
    // io.sram3_wen    := !io.enw
    // io.sram3_wmask  := 0.U
    // io.sram3_wdata  := io.wdata

    io.data := 0.U
    switch(io.index(7, 6))
    {
        is("b00".U)
        {
            RAM0.io.CEN := 0.U
            io.data := RAM0.io.Q
            // io.sram0_cen := 0.U
            // io.data := io.sram0_rdata
        }
        is("b01".U)
        {
            RAM1.io.CEN := 0.U
            io.data := RAM1.io.Q
            // io.sram1_cen := 0.U
            // io.data := io.sram1_rdata
        }
        is("b10".U)
        {
            RAM2.io.CEN := 0.U
            io.data := RAM2.io.Q
            // io.sram2_cen := 0.U
            // io.data := io.sram2_rdata
        }
        is("b11".U)
        {
            RAM3.io.CEN := 0.U
            io.data := RAM3.io.Q
            // io.sram3_cen := 0.U
            // io.data := io.sram3_rdata
        }
    }

    val TAG_VEC = RegInit(VecInit(Seq.fill(256)(0.U(54.W))))
    // val TAG_VEC = RegEnable(Cat(io.in_valid, io.in_dirty, io.in_tag), VecInit(Seq.fill(256)(0.U(54.W))), io.enMEM2WB)
    io.valid := TAG_VEC(io.index)(53)
    io.dirty := TAG_VEC(io.index)(52)
    io.tag   := TAG_VEC(io.index)(51,0)
    TAG_VEC(io.index)  := Mux(io.tag_enw, Cat(io.in_valid, io.in_dirty, io.in_tag), TAG_VEC(io.index))
}