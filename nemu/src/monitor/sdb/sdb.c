/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <isa.h>
#include <cpu/cpu.h>
#include <readline/readline.h>
#include <readline/history.h>
#include "sdb.h"
#include <memory/paddr.h>

static int is_batch_mode = false;

void init_regex();
void init_wp_pool();

/* We use the `readline' library to provide more flexibility to read from stdin. */
static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(nemu) ");

  if (line_read && *line_read) {
    add_history(line_read);
  }

  return line_read;
}

static int cmd_p(char *args) 
{
  bool *expr_flag = (bool*) malloc(sizeof(bool));
  word_t expr_result = expr(args, expr_flag);
  assert(*expr_flag);
  free(expr_flag);
  printf("The result is:%lu, hex format:0x%016lX\n", expr_result, expr_result);
  return 0;
}

static int cmd_c(char *args) {
  cpu_exec(-1);
  return 0;
}

static int cmd_w(char *args) 
{
  new_wp(args);
  return 0;
}

static int cmd_x(char *args)
{
  const char *x_N = strtok(args, " ");
  const char *x_EXPR = strtok(NULL, " ");

  int byte_c = 0;
  int byte4_c = 0;
  int x_N_num;
  word_t x_EXPR_num;
  //Log("%s\n%s", x_N, x_EXPR);

  sscanf(x_N, "%i", &x_N_num);
  sscanf(x_EXPR, "%lX", &x_EXPR_num);
  for(byte4_c = 0;byte4_c < x_N_num; byte4_c++)
  {
    printf("0x%016lx: ", x_EXPR_num);
    for(byte_c=0; byte_c <= 3; byte_c++)
    {
      printf("%02lx ", vaddr_read(x_EXPR_num+byte_c, 1));
    }
    printf("\t");
    for(byte_c=0; byte_c <= 3; byte_c++)
    {
      printf("\'%c\'", (char) vaddr_read(x_EXPR_num+byte_c, 1));
    }
    x_EXPR_num += 4;
    printf("\n");
  }
  return 0;
}

static int cmd_info(char *args)
{
  const char *info_r = "r"; //print registers' value
  const char *info_w = "w";

  if(strcmp(args, info_r) == 0) isa_reg_display();
  else if(strcmp(args, info_w) == 0) display_wp();
  else printf("wrong parameter\n");
  return 0;
}

static int cmd_si(char *args)
{
  //注意args为空的情况需要进行缺省设置
  int si_steps = 0;
  if(args == NULL) si_steps = 1;
  else si_steps = atoi(args);
  cpu_exec(si_steps);
  return 0;
}

static int cmd_q(char *args) 
{
  nemu_state.state = NEMU_QUIT;
  return -1;
}

static int cmd_help(char *args);

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "help", "Display informations about all supported commands", cmd_help },
  { "c", "Continue the execution of the program", cmd_c },
  { "q", "Exit NEMU", cmd_q },
  { "si", "", cmd_si},
  { "info", "", cmd_info},
  { "x", "", cmd_x},
  { "p", "", cmd_p},
  { "w", "", cmd_w},

  /* TODO: Add more commands */

};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_help(char *args) {
  /* extract the first argument */
  char *arg = strtok(NULL, " ");
  int i;

  if (arg == NULL) {
    /* no argument given */
    for (i = 0; i < NR_CMD; i ++) {
      printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
    }
  }
  else {
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(arg, cmd_table[i].name) == 0) {
        printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
        return 0;
      }
    }
    printf("Unknown command '%s'\n", arg);
  }
  return 0;
}

void sdb_set_batch_mode() {
  is_batch_mode = true;
}

void sdb_mainloop() {
  if (is_batch_mode) {
    cmd_c(NULL);
    return;
  }

  for (char *str; (str = rl_gets()) != NULL; ) {
    char *str_end = str + strlen(str);

    /* extract the first token as the command */
    char *cmd = strtok(str, " ");
    if (cmd == NULL) { continue; }

    /* treat the remaining string as the arguments,
     * which may need further parsing
     */
    char *args = cmd + strlen(cmd) + 1;
    if (args >= str_end) {
      args = NULL;
    }

#ifdef CONFIG_DEVICE
    extern void sdl_clear_event_queue();
    sdl_clear_event_queue();
#endif

    int i;
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(cmd, cmd_table[i].name) == 0) {
        if (cmd_table[i].handler(args) < 0) { return; }
        break;
      }
    }

    if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
  }
}

void init_sdb() {
  /* Compile the regular expressions. */
  init_regex();

  /* Initialize the watchpoint pool. */
  init_wp_pool();
}
