#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int printf(const char *fmt, ...) 
{
  char result[4096]={};
  char *out = result;
  assert(out != NULL);
  assert(fmt != NULL);
  int count = 0;
  char c;
  char *s;
  int n;
  
  // int index = 0;
  // int ret = 2;
  
  char buf[65];
  char digit[16];
  // int num = 0;
  // int len = 0;
  
  memset(buf, 0, sizeof(buf));
  memset(digit, 0, sizeof(digit));

  va_list ap;
  va_start(ap, fmt);
  while(*fmt != '\0')
  {
    if(*fmt == '%')
    {
      fmt++;
      switch(*fmt)
      {
        case 'd': /*整型*/
        {
          n = va_arg(ap, int);
          if(n < 0)
          {
            *out = '-';
            out++;
            n = -n;
          }
          itoa(n, buf);
          memcpy(out, buf, strlen(buf));
          out += strlen(buf);
          break;
        }    
        case 'c': /*字符型*/
        {
          c = va_arg(ap, int);
          *out = c;
          out++;
          
          break;
        }
        case 'x': /*16进制*/
        {
          n = va_arg(ap, int);
          xtoa(n, buf);
          memcpy(out, buf, strlen(buf));
          out += strlen(buf);
          break;
        }
        case 's': /*字符串*/
        {
          s = va_arg(ap, char *);
          memcpy(out, s, strlen(s));
          out += strlen(s);
          break;
        }
        case '%': /*输出%*/
        {
          *out = '%';
          out++;
          break;
        }
        default:break;
      }
    }
    else
    {
      *out = *fmt;
      out++;
      if(*fmt == '\n')
      {   
      }
    }
  fmt++;
  }
  *out = '\0';
  va_end(ap);
  // putch('#');
  putstr(result);
  return count;
}

int vsprintf(char *out, const char *fmt, va_list ap) 
{
  panic("Not implemented");
}

int sprintf(char *out, const char *fmt, ...) 
{
  assert(out != NULL);
  assert(fmt != NULL);
  int count = 0;
  char c;
  char *s;
  int n;
  
  // int index = 0;
  // int ret = 2;
  
  char buf[65];
  char digit[16];
  // int num = 0;
  // int len = 0;
  
  memset(buf, 0, sizeof(buf));
  memset(digit, 0, sizeof(digit));

  va_list ap;
  va_start(ap, fmt);
  while(*fmt != '\0')
  {
    // printf("*fmt=[%c]\n", *fmt);
    if(*fmt == '%')
    {
      fmt++;
      switch(*fmt)
      {
        case 'd': /*整型*/
        {
          n = va_arg(ap, int);
          if(n < 0)
          {
            *out = '-';
            out++;
            n = -n;
          }
          // printf("case d n=[%d]\n", n);
          itoa(n, buf);
          // int test = atoi(buf);assert(n==test);
          // printf("case d buf=[%s]\n", buf);
          memcpy(out, buf, strlen(buf));
          out += strlen(buf);
          break;
        }    
        case 'c': /*字符型*/
        {
          c = va_arg(ap, int);
          *out = c;
          out++;
          
          break;
        }
        case 'x': /*16进制*/
        {
          n = va_arg(ap, int);
          xtoa(n, buf);
          memcpy(out, buf, strlen(buf));
          out += strlen(buf);
          break;
        }
        case 's': /*字符串*/
        {
          s = va_arg(ap, char *);
          memcpy(out, s, strlen(s));
          out += strlen(s);
          break;
        }
        case '%': /*输出%*/
        {
          *out = '%';
          out++;
          break;
        }
        default:break;
      }
    }
    else
    {
      *out = *fmt;
      out++;
      if(*fmt == '\n')
      {   
      }
    }
  fmt++;
  }
  *out = '\0';
  va_end(ap);
  return count;
  // putch('1');
  // panic("Not implemented");
}

int snprintf(char *out, size_t n, const char *fmt, ...) 
{
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) 
{
  panic("Not implemented");
}


#endif
