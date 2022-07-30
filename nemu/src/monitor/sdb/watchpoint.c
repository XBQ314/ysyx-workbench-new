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

#include "sdb.h"

#define NR_WP 32

typedef struct watchpoint {
  int NO;
  struct watchpoint *next;

  /* TODO: Add more members if necessary */
  char expr[128];
  word_t pre_val;
} WP;

static WP wp_pool[NR_WP] = {};
static WP *head = NULL, *free_ = NULL;
static WP *tail = NULL;

void init_wp_pool() 
{
  int i;
  for (i = 0; i < NR_WP; i ++) 
  {
    wp_pool[i].NO = i;
    wp_pool[i].next = (i == NR_WP - 1 ? NULL : &wp_pool[i + 1]);
  }

  head = NULL;
  free_ = wp_pool;
}

/* TODO: Implement the functionality of watchpoint */
WP* new_wp(char* usr_expr)
{
  if(head == NULL)
  {
    head = free_;
    tail = head;

    free_ = free_->next;
    tail->next = NULL;
    strcpy(tail->expr, usr_expr);

    bool *expr_flag = (bool*) malloc(sizeof(bool));
    tail->pre_val = expr(tail->expr, expr_flag);
    assert(*expr_flag);
    free(expr_flag);
  }
  else
  {
    if(free_ != NULL) tail->next = free_;
    else
    {
      Log("No more watchpoints can be used!");
      assert(0);
    }
    free_ = free_->next;
    tail = tail->next;
    tail->next = NULL;
    strcpy(tail->expr, usr_expr);

    bool *expr_flag = (bool*) malloc(sizeof(bool));
    tail->pre_val = expr(tail->expr, expr_flag);
    assert(*expr_flag);
    free(expr_flag);
  }
  Log("A New watchpoint has been put into use, the NO:%d, the pre value is:%lu", tail->NO, tail->pre_val);
  return tail;
}

void free_wp(WP *wp)
{
  WP *pre = head;
  if(wp == head)
  {
    head = head->next;
    wp->next = free_;
    free_ = wp;
  }
  else
  {
    while(pre->next != wp)
    {
      pre = pre->next;
    }
    pre->next = wp->next;
    wp->next = free_;
    free_ = wp;
  }
  Log("A watchpoint has been retired, the NO is:%d, the pre value is:%lu", wp->NO, wp->pre_val);
  return;
}

void free_wp_n(int n)
{
  WP *nod = head;
  while(nod->NO != n)
  {
    nod = nod->next;
  }
  free_wp(nod);
  return;
}

void check_wp()
{
  WP *nod = head;
  word_t cur_val = 0;
  if(nod == NULL) return;
  while(nod != NULL)
  {
    bool *expr_flag = (bool*) malloc(sizeof(bool));
    cur_val = expr(nod->expr, expr_flag);
    assert(*expr_flag);
    free(expr_flag);

    if(cur_val != nod->pre_val)
    {
      nemu_state.state = NEMU_STOP;
      Log("Watchpoint NO:%d has deteceted a change, expr \"%s\" change from %lu to %lu, call pause", nod->NO, nod->expr, nod->pre_val, cur_val);
      nod->pre_val = cur_val;
      break;
    }
    nod = nod->next;
  }
  return;
}

void display_wp()
{
  WP *nod = head;
  if(head != NULL)
  {
    printf("Number\tExpr\t\t\t\tValue\t\n");
    while(nod != NULL)
    {
      printf("%d\t%s\t\t\t\t%lu\t\n", nod->NO, nod->expr, nod->pre_val);
      nod = nod->next;
    }
  }
  return;
}

