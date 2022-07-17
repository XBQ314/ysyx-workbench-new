#include <isa.h>

/* We use the POSIX regex functions to process regular expressions.
 * Type 'man regex' for more information about POSIX regex functions.
 */
#include <regex.h>

static bool check_parentheses(int p, int q);
static int find_op(int p, int q);
static bool check_expr(int p, int q);
static word_t eval(int p, int q);

enum {
  TK_NOTYPE = 256, TK_EQ,

  /* TODO: Add more token types */
  TK_NUM, TK_HEX, TK_REG, TK_NEQ, TK_AND, TK_DEREF, TK_VAR, TK_GE, TK_SE
};

static struct rule {
  const char *regex;
  int token_type;
} rules[] = {

  /* TODO: Add more rules.
   * Pay attention to the precedence level of different rules.
   */

  {" +", TK_NOTYPE},    // spaces
  {"\\+",'+'},// plus
  {"\\-",'-'},// minus
  {"\\*",'*'},// product
  {"\\/",'/'},// divide
  {"\\(",'('},
  {"\\)",')'},
  {"^[a-zA-Z_][a-zA-Z0-9_]*$", TK_VAR},//variable
  {"0[xX][0-9a-e]*", TK_HEX},
  {"[0-9]+",TK_NUM},// number
  {"\\$..[01]?", TK_REG},
  {"==", TK_EQ},// equal
  {">=", TK_GE},
  {"<=", TK_SE},
  {"!=", TK_NEQ},
  {"&&", TK_AND}
};

#define NR_REGEX ARRLEN(rules)

static regex_t re[NR_REGEX] = {};

/* Rules are used for many times.
 * Therefore we compile them only once before any usage.
 */
void init_regex() {
  int i;
  char error_msg[128];
  int ret;

  for (i = 0; i < NR_REGEX; i ++) {
    ret = regcomp(&re[i], rules[i].regex, REG_EXTENDED);
    if (ret != 0) {
      regerror(ret, &re[i], error_msg, 128);
      panic("regex compilation failed: %s\n%s", error_msg, rules[i].regex);
    }
  }
}

typedef struct token {
  int type;
  char str[32];
} Token;

static Token tokens[65536] __attribute__((used)) = {};
static int nr_token __attribute__((used))  = 0;
// #define TOKENS_NUM ARRLEN(tokens) 

static bool make_token(char *e) {
  int position = 0;
  int i;
  int j;
  regmatch_t pmatch;

  nr_token = 0;

  // printf("number of rules:%d\n", NR_REGEX);
  // for(i = 0; i < NR_REGEX; i++)
  // {
  //   printf("rules[%d] is \"%s\"\n", i, rules[i].regex);
  // }

  while (e[position] != '\0') {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i ++) {
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0) {
        char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        // printf("match rules[%d] = \"%s\" at position %d with len %d: %.*s\n",
        //     i, rules[i].regex, position, substr_len, substr_len, substr_start);

        position += substr_len;

        /* TODO: Now a new token is recognized with rules[i]. Add codes
         * to record the token in the array `tokens'. For certain types
         * of tokens, some extra actions should be performed.
         */

        switch (rules[i].token_type) 
        {
          case TK_NOTYPE:// spaces
            break;
          case '+':// plus
            tokens[nr_token].type = '+';
            tokens[nr_token++].str[0] = '+';
            break;
          case '-':// minus
            tokens[nr_token].type = '-';
            tokens[nr_token++].str[0] = '-';
            break;         
          case '*':// product
            tokens[nr_token].type = '*';
            tokens[nr_token++].str[0] = '*';
            break; 
          case '/':// divide
            tokens[nr_token].type = '/';
            tokens[nr_token++].str[0] = '/';
            break; 
          case '(':
            tokens[nr_token].type = '(';
            tokens[nr_token++].str[0] = '(';
            break;
          case ')':
            tokens[nr_token].type = ')';
            tokens[nr_token++].str[0] = ')';
            break;
          case TK_VAR:
            tokens[nr_token].type = TK_VAR;
            for(j = 0; j < substr_len; j++)
            {
              tokens[nr_token].str[j] = substr_start[j];
            }
            tokens[nr_token++].str[j] = '\0';
            break;
          case TK_HEX:
            tokens[nr_token].type = TK_HEX;
            for(j = 0; j < substr_len; j++)
            {
              tokens[nr_token].str[j] = substr_start[j];
            }
            tokens[nr_token++].str[j] = '\0';
            break;
          case TK_REG:
            tokens[nr_token].type = TK_REG;
            for(j = 0; j < substr_len - 1; j++)
            {
              tokens[nr_token].str[j] = substr_start[j+1];
            }
            tokens[nr_token++].str[j] = '\0';
            break;
          case TK_NUM:// number
            tokens[nr_token].type = TK_NUM;
            for(j = 0; j < substr_len; j++)
            {
              tokens[nr_token].str[j] = substr_start[j];
            }
            tokens[nr_token++].str[j] = '\0';
            break;
          case TK_EQ: // equal
            tokens[nr_token].type = TK_EQ;
            tokens[nr_token].str[0] = '=';
            tokens[nr_token].str[1] = '=';
            tokens[nr_token++].str[2] = '\0';
            break;
          case TK_NEQ:
            tokens[nr_token].type = TK_NEQ;
            tokens[nr_token].str[0] = '!';
            tokens[nr_token].str[1] = '=';
            tokens[nr_token++].str[2] = '\0';
            break;
          case TK_GE:
            tokens[nr_token].type = TK_GE;
            tokens[nr_token].str[0] = '>';
            tokens[nr_token].str[1] = '=';
            tokens[nr_token++].str[2] = '\0';
            break;
          case TK_SE:
            tokens[nr_token].type = TK_SE;
            tokens[nr_token].str[0] = '<';
            tokens[nr_token].str[1] = '=';
            tokens[nr_token++].str[2] = '\0';
            break;
          case TK_AND:
            tokens[nr_token].type = TK_AND;
            tokens[nr_token].str[0] = '&';
            tokens[nr_token].str[1] = '&';
            tokens[nr_token++].str[2] = '\0';
          default: break;
        }

        break;
      }
    }

    if (i == NR_REGEX) {
      printf("no match at position %d\n%s\n%*.s^\n", position, e, position, "");
      return false;
    }
  }

  for(i = 0; i<=nr_token; i++)
  {
    // printf("tokens%d: type:%d\tstr:%s\n", i, tokens[i].type, tokens[i].str);
  }

  return true;
}

word_t expr(char *e, bool *success) 
{
  if (!make_token(e)) 
  {
    *success = false;
    return 0;
  }

  /* TODO: Insert codes to evaluate the expression. */
  // TODO();
  // DEREF or not
  int i = 0;
  for (i = 0; i < nr_token; i ++) 
  {
    if (tokens[i].type == '*' && 
    (i == 0 ||  
    tokens[i-1].type == '+' ||
    tokens[i-1].type == '-' ||
    tokens[i-1].type == '*' ||
    tokens[i-1].type == '/' ||
    tokens[i-1].type == TK_AND ||
    tokens[i-1].type == TK_NEQ ||
    tokens[i-1].type == TK_EQ ||
    tokens[i-1].type == TK_GE ||
    tokens[i-1].type == TK_SE)) 
    {
      tokens[i].type = TK_DEREF;
    }
  }


  nr_token--;
  // printf("Begin to eval the expr. The q=%d\n", nr_token);
  word_t expr_result = eval(0, nr_token);
  // printf("The result is:%lu, hex format:0x%016lX\n", expr_result, expr_result);
  *success = true;
  
  // int i = 0;
  // for(i = 0; i<TOKENS_NUM; i++)
  // {
  //   tokens[i].type = ;
  //   tokens[i].str = ;
  // }
  return expr_result;
}

static bool check_parentheses(int p, int q) 
{
  int unmatch_leftpt = 0;
  while(p<=q)
  {
    if(tokens[p].type == '(' )
    {
      unmatch_leftpt++;
    }
    else if(tokens[p].type == ')' )
    {
      unmatch_leftpt--;
    }
    if(unmatch_leftpt == 0)
    {
      // Log("unmatched ( become zero. now p=%d, q=%d", p, q);
      break;
    }
    p++;
  }
  if(p != q || unmatch_leftpt != 0)
  {
    // Log("check pt return FALSE! now p=%d, q=%d unmatched leftpt=%d", p, q, unmatch_leftpt);
    return false;
  }
  // Log("check pt return TRUE! now p=%d, q=%d unmatched leftpt=%d", p, q, unmatch_leftpt);
  return true;
}

static int find_op(int p, int q)
{
  int op = p;
  int unmatch_leftpt = 0;
  bool as_flag = false; //have add or sub
  bool equal_flag = false; //have equal or not equal
  bool gs_flag = false; //have greater or smaller 
  bool and_flag = false; // have and

  while(p <= q)
  {
    if(tokens[p].type == '(' )
    {
      unmatch_leftpt++;
    }
    else if(tokens[p].type == ')' )
    {
      unmatch_leftpt--;
    }

    if(unmatch_leftpt == 0)
    {
      if((tokens[p].type == TK_AND) && 
          p > op)
      {
        // Log("find op:%s at position:%d", tokens[p].str, p);
        and_flag = true;
        op = p;
      }
      else if((and_flag == false) && 
              (tokens[p].type == TK_EQ || tokens[p].type == TK_NEQ) && 
              p > op)
      {
        // Log("find op:%s at position:%d", tokens[p].str, p);
        equal_flag = true;
        op = p;
      }
      else if((and_flag == false) && 
              (equal_flag == false) && 
              (tokens[p].type == TK_GE || tokens[p].type == TK_SE) && 
              p > op)
      {
        // Log("find op:%s at position:%d", tokens[p].str, p);
        gs_flag = true;
        op = p;
      }
      else if((and_flag == false) && 
              (equal_flag == false) && 
              (gs_flag == false) &&
              (tokens[p].type == '+' || tokens[p].type == '-') && 
              p > op)
      {
        // Log("find op:%c at position:%d", tokens[p].str[0], p);
        as_flag = true;
        op = p;
      }
      else if((and_flag == false) && 
              (equal_flag == false) && 
              (gs_flag == false) &&
              (as_flag == false) && 
              (tokens[p].type == '*' || tokens[p].type == '/') && 
              p>op)
      {
        // Log("find op:%c at position:%d", tokens[p].str[0], p);
        op = p;
      }
    }
    p++;
  }
  // Log("the op is at position:%d;", op);
  return op;
}

static bool check_expr(int p, int q)
{
  int unmatch_leftpt = 0;
  while(p<=q)
  {
    if(tokens[p].type == '(' )
    {
      unmatch_leftpt++;
    }
    else if(tokens[p].type == ')' )
    {
      unmatch_leftpt--;
    }
    p++;
  }
  if(unmatch_leftpt != 0)
  {
    // Log("() not matched");
    return false;
  }
  return true;
}

static word_t eval(int p, int q) 
{
  if (check_expr(p, q) == false) assert(0);
  if (p > q) 
  {
    /* Bad expression */
    Log("p>q!!!expr eval error!");
    assert(0);
  }
  else if (p == q) 
  {
    /* Single token.
     * For now this token should be a number.
     * Return the value of the number.
     */
    assert((tokens[p].type == TK_NUM) || tokens[p].type == TK_HEX || tokens[p].type == TK_REG);
    if(tokens[p].type == TK_NUM)
    {
      // Log("find num:%lu", strtoul(tokens[p].str, NULL, 10));
      return strtoul(tokens[p].str, NULL, 10);
    }
    else if(tokens[p].type == TK_HEX)
    {
      // Log("find hex num:%lu", strtoul(tokens[p].str, NULL, 16));
      return strtoul(tokens[p].str, NULL, 16);
    }
    else if(tokens[p].type == TK_REG)
    {
      bool *reg_flag = (bool*) malloc(sizeof(bool));
      word_t reg_value = isa_reg_str2val(tokens[p].str, reg_flag);
      assert(*reg_flag);
      free(reg_flag);
      return reg_value;
    }
  }
  else if (check_parentheses(p, q) == true) 
  {
    /* The expression is surrounded by a matched pair of parentheses.
     * If that is the case, just throw away the parentheses.
     */
    return eval(p + 1, q - 1);
  }
  else 
  {
    int op = find_op(p, q);//the position of 主运算符 in the token expression;
    word_t val1 = eval(p, op - 1);
    word_t val2 = eval(op + 1, q);

    switch (tokens[op].type) 
    {
      case '+': return val1 + val2;
      case '-': return val1 - val2;
      case '*': return val1 * val2;
      case '/': return val1 / val2;
      case TK_EQ: return val1 == val2;
      case TK_NEQ: return val1 != val2;
      case TK_GE: return val1 >= val2;
      case TK_SE: return val1 <= val2;
      case TK_AND: return val1 && val2;
      default: assert(0);
    }
  }
  return 0;
}