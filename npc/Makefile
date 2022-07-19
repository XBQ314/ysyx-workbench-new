# 编译生成的文件存储的目录
BUILD_DIR += ./build
OBJ_DIR += $(BUILD_DIR)/obj_dir
CHISEL_OBJ_DIR += $(BUILD_DIR)/chisel_dir

# sim用的编译器选项
VERILATOR += verilator
# VERILATOR_FLAGS += -Wall
VERILATOR_FLAGS += --trace
VERILATOR_FLAGS += --cc --exe --build

# nvboard用的编译器选项
VERILATOR_CFLAGS += -MMD --build -cc  \
				-O3 --x-assign fast --x-initial fast --noassert

# include $(NVBOARD_HOME)/scripts/nvboard.mk

INC_PATH ?=
INCFLAGS = $(addprefix -I, $(INC_PATH))
CFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""
LDFLAGS += -lSDL2 -lSDL2_image

# nvboard用的约束文件生成
NXDC_FILES = constr/top.nxdc
SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
$(SRC_AUTO_BIND): $(NXDC_FILES)
	@echo "generating xdc file $(SRC_AUTO_BIND)"
	python $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

# 源文件
MODULENAME = RV64Top
# MODULENAME = RegisterFiles
TOPNAME = FlyingLed
VSRCS_SIM += $(shell find $(abspath ./build/chisel_dir) -name "*.v")
VSRCS_NVBOARD += $(VSRCS_SIM)
CSRCS_SIM += $(shell find $(abspath ./playground/test/src/csrc) -name "tb_RV64Top.cpp")
# CSRCS_SIM += $(shell find $(abspath ./playground/test/src/csrc) -name "tb_RegisterFiles.cpp")
CSRCS_SIM += $(shell find $(abspath ./playground/test/src/csrc/memory) -name "*.cpp")
CSRCS_SIM += $(shell find $(abspath ./playground/test/src/csrc/monitor) -name "*.cpp")
CSRCS_SIM += $(shell find $(abspath ./playground/test/src/csrc/cpu) -name "*.cpp")
CSRCS_SIM += $(shell find $(abspath ./playground/test/src/csrc/cpu/difftest) -name "*.cpp")
CSRCS_SIM += $(shell find $(abspath ./playground/test/src/csrc/utils) -name "*.cpp")
CSRCS_SIM += $(shell find $(abspath ./playground/test/src/csrc/utils) -name "*.cc")
CSRCS_NVBOARD += $(shell find $(abspath ./playground/test/src/csrc) -name "board_main.cpp")
CSRCS_NVBOARD += $(SRC_AUTO_BIND)

# 目标文件
BIN_SIM += $(BUILD_DIR)/V$(MODULENAME)
BIN_NVBOARD += $(BUILD_DIR)/$(TOPNAME)

test:
	mill -i __.test

verilog:
	$(call git_commit, "generate verilog")
	mkdir -p $(CHISEL_OBJ_DIR)
	mill -i __.test.runMain Elaborate -td $(CHISEL_OBJ_DIR)

help:
	mill -i __.test.runMain Elaborate --help

compile:
	mill -i __.compile

bsp:
	mill -i mill.bsp.BSP/install

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

clean:
	-rm -rf $(BUILD_DIR)

.PHONY: test verilog help compile bsp reformat checkformat clean

sim:$(BIN_SIM)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by yourself."
	@echo "SIM BIN generated successfully!."
	@$(BIN_SIM)
	@echo "SIM BIN excuted successfully!."

run:$(BIN_SIM)
	@echo "this is npc run"
	@echo $(IMG)
	$(BIN_SIM) $(IMG)


nvboard:$(BIN_NVBOARD)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "nvboard bin generate ok"

# 仿真执行文件生成
# 这一行是C语言所需要的头文件链接路径 $(addprefix -CFLAGS , -I$(abspath ./playground/test/src/csrc/include/))
# 这一行是C语言的动态链接库参数 $(addprefix -LDFLAGS , -lreadline)
# $(addprefix -LDFLAGS , -ldl -fsanitize=address)是让其正确链接nemu的动态库
$(BIN_SIM):$(VSRCS_SIM) $(CSRCS_SIM)
	@ rm -rf $(OBJ_DIR)
	@ $(shell mkdir -p $(BUILD_DIR))
	@ $(VERILATOR) $(VERILATOR_FLAGS) \
	--Mdir $(OBJ_DIR) \
	-top $(MODULENAME) \
	$(VSRCS_SIM) $(CSRCS_SIM) \
	$(addprefix -CFLAGS , -I$(abspath ./playground/test/src/csrc/include/)) \
	$(addprefix -CFLAGS , $(shell llvm-config --cxxflags) -fPIE) \
	$(addprefix -LDFLAGS , -lreadline) \
	$(addprefix -LDFLAGS , -ldl -fsanitize=address) \
	$(addprefix -LDFLAGS , $(shell llvm-config --libs)) \
	-o $(abspath $(BIN_SIM))

# $(addprefix -LDFLAGS , -Ldir $(abspath ../nemu/build/)) \
# CXXFLAGS += $(shell llvm-config --cxxflags) -fPIE
# LIBS += $(shell llvm-config --libs)
# riscv64-nemu-interpreter-so

# nvboard执行文件生成
$(BIN_NVBOARD):$(VSRCS_NVBOARD) $(CSRCS_NVBOARD) $(NVBOARD_ARCHIVE)
	rm -rf $(OBJ_DIR)
	$(shell mkdir -p $(BUILD_DIR))
	$(VERILATOR) $(VERILATOR_CFLAGS) \
	-top $(TOPNAME) \
	$(VSRCS_NVBOARD) $(CSRCS_NVBOARD) $(NVBOARD_ARCHIVE) \
	$(addprefix -CFLAGS , $(CFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
	--Mdir $(OBJ_DIR) \
	--exe -o $(abspath $(BIN_NVBOARD))

include ../Makefile
