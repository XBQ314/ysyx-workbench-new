#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

size_t strlen(const char *s) 
{
  size_t r = 0;
  assert(s != NULL);
	while (*s)
	{
		s++;
		r++;
	}
	return r;
  // panic("Not implemented");
}

char *strcpy(char *dst, const char *src) 
{
  char *r = dst;
  assert(dst != NULL);
  assert(src != NULL);
  while((*dst++ = *src++))
  {
  ;
  }
  return r;
  // panic("Not implemented");
}

char *strncpy(char *dst, const char *src, size_t n) 
{
  panic("Not implemented");
}

char *strcat(char *dst, const char *src) // 拼接字符串
{
  char *r = dst;
	while (*dst)
	{
		dst++;
	}
	while ((*dst++=*src++))
	{
		;
	}
	return r;
  // panic("Not implemented");
}

int strcmp(const char *s1, const char *s2) 
{
  while((*s1!='\0')&&(*s1==*s2))
  {
      s1++;
      s2++;
  }
  int r;
  r=*s1-*s2;
  return r;
  // panic("Not implemented");
}

int strncmp(const char *s1, const char *s2, size_t n) 
{
  panic("Not implemented");
}

void *memset(void *s, int c, size_t n) 
{
  assert(s != NULL);
  assert(n > 0);
  size_t i = 0;
  char *r = (char *)s;
  for(; i < n; i++)
  {
    r[i] = c;
  }
  return s;
  // panic("Not implemented");
}

void *memmove(void *dst, const void *src, size_t n) {
  panic("Not implemented");
}

void *memcpy(void *out, const void *in, size_t n) 
{
  void * ret = out;
  assert(out);
  assert(in);
  /*
   * copy from lower addresses to higher addresses
   */
  while (n--) 
  {
    *(char *)out = *(char *)in;
    out = (char *)out + 1;
    in = (char *)in + 1;
  }
 
  return ret;
  // panic("Not implemented");
}

int memcmp(const void *s1, const void *s2, size_t n) 
{
  assert(s1 != NULL);
  assert(s1 != NULL);
  assert(n >= 0);if(n == 0)return 0;
  char *r1 = (char *)s1;
  char *r2 = (char *)s2;

  while(*r1 == *r2 && --n>0)
  {
      r1++;
      r2++;
  }
  int r=*r1-*r2;
  return r;
  // panic("Not implemented");
}

#endif