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

    RAM1.io.CLK := io.CLK
    RAM1.io.CEN := 1.U
    RAM1.io.WEN := !io.enw
    RAM1.io.A   := io.index(5, 0)
    RAM1.io.D   := io.wdata

    RAM2.io.CLK := io.CLK
    RAM2.io.CEN := 1.U
    RAM2.io.WEN := !io.enw
    RAM2.io.A   := io.index(5, 0)
    RAM2.io.D   := io.wdata

    RAM3.io.CLK := io.CLK
    RAM3.io.CEN := 1.U
    RAM3.io.WEN := !io.enw
    RAM3.io.A   := io.index(5, 0)
    RAM3.io.D   := io.wdata

    io.data := 0.U
    switch(io.index(7, 6))
    {
        is("b00".U)
        {
            RAM0.io.CEN := 0.U
            io.data := RAM0.io.Q
        }
        is("b01".U)
        {
            RAM1.io.CEN := 0.U
            io.data := RAM1.io.Q
        }
        is("b10".U)
        {
            RAM2.io.CEN := 0.U
            io.data := RAM2.io.Q
        }
        is("b11".U)
        {
            RAM3.io.CEN := 0.U
            io.data := RAM3.io.Q
        }
    }

    val TAG_VEC = RegInit(VecInit(Seq.fill(256)(0.U(54.W))))
    // val TAG_VEC = RegEnable(Cat(io.in_valid, io.in_dirty, io.in_tag), VecInit(Seq.fill(256)(0.U(54.W))), io.enMEM2WB)
    io.valid := TAG_VEC(io.index)(53)
    io.dirty := TAG_VEC(io.index)(52)
    io.tag   := TAG_VEC(io.index)(51,0)
    TAG_VEC(io.index)  := Mux(io.tag_enw, Cat(io.in_valid, io.in_dirty, io.in_tag), TAG_VEC(io.index))
}