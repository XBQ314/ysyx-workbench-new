#include "sdb.h"
#include "cpu.h"
#include "utils.h"
#include "reg.h"
#include "mem.h"
#include <readline/readline.h>
#include <readline/history.h>

static char* rl_gets() 
{
  static char *line_read = NULL;

  if (line_read) 
  {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(npc) ");

  if (line_read && *line_read) 
  {
    add_history(line_read);
  }

  return line_read;
}

static int cmd_d(char *args) 
{
  free_wp_n(atoi(args));
  return 0;
}

static int cmd_w(char *args) 
{
  new_wp(args);
  return 0;
}

static int cmd_p(char *args) 
{
  bool *expr_flag = (bool*) malloc(sizeof(bool));
  uint64_t result = expr(args, expr_flag);
  assert(*expr_flag);
  printf("The result is:%lu, hex format:0x%016lX\n", result, result);
  free(expr_flag);
  return 0;
}

static int cmd_q(char *args) 
{
  npc_state.state = NPC_QUIT;
  return -1;
}

static int cmd_x(char *args)
{
  const char *x_N = strtok(args, " ");
  const char *x_EXPR = strtok(NULL, " ");

  int byte_c = 0;
  int byte4_c = 0;
  int x_N_num;
  uint64_t x_EXPR_num;
  //Log("%s\n%s", x_N, x_EXPR);

  sscanf(x_N, "%i", &x_N_num);
  sscanf(x_EXPR, "%lX", &x_EXPR_num);
  for(byte4_c = 0;byte4_c < x_N_num; byte4_c++)
  {
    printf("0x%016lx: ", x_EXPR_num);
    for(byte_c=0; byte_c <= 3; byte_c++)
    {
      printf("%02x ", mem_read(x_EXPR_num+byte_c, 1));
    }
    printf("\t");
    for(byte_c=0; byte_c <= 3; byte_c++)
    {
      printf("\'%c\'", (char) mem_read(x_EXPR_num+byte_c, 1));
    }
    x_EXPR_num += 4;
    printf("\n");
  }
  return 0;
}

static int cmd_c(char *args) 
{
  cpu_exec(-1);
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

static int cmd_info(char *args)
{
  const char *info_r = "r"; //print registers' value
  const char *info_w = "w";
  const char *info_csr = "csr";

  if(strcmp(args, info_r) == 0) display_reg();
  else if(strcmp(args, info_w) == 0) display_wp();
  else if(strcmp(args, info_csr) == 0) display_csr();
  else printf("wrong parameter\n");
  return 0;
}

static int cmd_help(char * args)
{
    printf("help!\n");
    return 0;
}

static struct 
{
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "help", "Display informations about all supported commands", cmd_help },
  { "c", "Continue the execution of the program", cmd_c },
  { "q", "Exit NPC", cmd_q },

  /* TODO: Add more commands */
//   {"test", "just test", cmd_test},
  {"si", "step exec the program", cmd_si},
  {"info", "print the program state", cmd_info},
  {"x", "求出表达式EXPR的值, 将结果作为起始内存地址, 以十六进制形式输出连续的N个4字节", cmd_x},
  {"p", "p EXPR 表达式EXPR的值", cmd_p},
  {"w", " ", cmd_w},
  {"d", " ", cmd_d}
};
#define ARRLEN(arr) (int)(sizeof(arr) / sizeof(arr[0]))
#define NR_CMD ARRLEN(cmd_table)

void sdb_mainloop() 
{
  cmd_c(NULL);
  return;
  // for (char *str; (str = rl_gets()) != NULL; ) 
  // {
  //   char *str_end = str + strlen(str);

  //   /* extract the first token as the command */
  //   char *cmd = strtok(str, " ");
  //   if (cmd == NULL) { continue; }

  //   /* treat the remaining string as the arguments,
  //    * which may need further parsing
  //    */
  //   char *args = cmd + strlen(cmd) + 1;
  //   if (args >= str_end) 
  //   {
  //     args = NULL;
  //   }

  //   int i;
  //   for (i = 0; i < NR_CMD; i ++) 
  //   {
  //     if (strcmp(cmd, cmd_table[i].name) == 0) 
  //     {
  //       if (cmd_table[i].handler(args) < 0) { return; }
  //       break;
  //     }
  //   }

  //   if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
  // }
}