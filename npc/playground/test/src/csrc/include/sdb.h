#ifndef _SDB_H_
#define _SDB_H

#include <stdint.h>
#include <stdbool.h>
#include <assert.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>

typedef struct watchpoint WP;
void init_wp_pool();
void free_wp(WP *wp);
WP* new_wp(char* usr_expr);
void free_wp_n(int n);
void check_wp();
void display_wp();

void check_ebreak();
void sdb_mainloop();

void init_regex();
uint64_t expr(char *e, bool *success);

#endif