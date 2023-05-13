#include <fs.h>

typedef size_t (*ReadFn) (void *buf, size_t offset, size_t len);
typedef size_t (*WriteFn) (const void *buf, size_t offset, size_t len);

extern size_t serial_write(const void *buf, size_t offset, size_t len);
extern size_t ramdisk_read(void *buf, size_t offset, size_t len);
extern size_t ramdisk_write(const void *buf, size_t offset, size_t len);
extern size_t events_read(void *buf, size_t offset, size_t len);
extern size_t dispinfo_read(void *buf, size_t offset, size_t len);
extern size_t fb_write(const void *buf, size_t offset, size_t len);

typedef struct 
{
  char *name;
  size_t size;
  size_t disk_offset;
  ReadFn read;
  WriteFn write;

  int open_offset;
} Finfo;

enum {FD_STDIN, FD_STDOUT, FD_STDERR, FD_FB};

size_t invalid_read(void *buf, size_t offset, size_t len) 
{
  panic("should not reach here");
  return 0;
}

size_t invalid_write(const void *buf, size_t offset, size_t len) 
{
  panic("should not reach here");
  return 0;
}

/* This is the information about all files in disk. */
/*没有显示制定读写函数指针的文件，默认使用ramdisk进行读写操作*/
static Finfo file_table[] __attribute__((used)) = 
{
  [FD_STDIN]  = {"stdin", 0, 0, invalid_read, invalid_write},
  [FD_STDOUT] = {"stdout", 0, 0, invalid_read, serial_write},
  [FD_STDERR] = {"stderr", 0, 0, invalid_read, serial_write},
  [FD_FB] = {"/dev/fb", 0, 0, invalid_read, fb_write},
  {"/dev/events", 0, 0, events_read, invalid_write},
  {"/proc/dispinfo", 0, 0, dispinfo_read, invalid_write},
#include "files.h"
};


int fs_open(const char *pathname, int flags, int mode)
{
  int i = 0;
  while(strcmp(pathname, file_table[i].name) != 0)
  {
    i++;
  }
  if(strcmp(pathname, file_table[i].name) != 0)
  {
    assert(0);
  }
  else
  {   
    file_table[i].open_offset=0;
    return i;
  }
}

size_t fs_read(int fd, void *buf, size_t len)
{
  if(file_table[fd].write)
  {
    return file_table[fd].read(buf, file_table[fd].disk_offset, len);
  }
  else
  {
    ramdisk_read(buf, file_table[fd].disk_offset + file_table[fd].open_offset, len);
    if(file_table[fd].open_offset + len > file_table[fd].size)len=file_table[fd].size-file_table[fd].open_offset;
    file_table[fd].open_offset+=len;
    return len;
  }
}


size_t fs_write(int fd, const void *buf, size_t len)
{
  // printf("This is fs_write, fd=%d, len=%d\n", fd, len);
  if(file_table[fd].write)
  {
    return file_table[fd].write(buf, file_table[fd].open_offset, len);
  }
  else
  {
    ramdisk_write(buf, file_table[fd].disk_offset + file_table[fd].open_offset, len);
    if(file_table[fd].open_offset + len > file_table[fd].size)len=file_table[fd].size-file_table[fd].open_offset;
    file_table[fd].open_offset+=len;
    return len;
  }

}

size_t fs_lseek(int fd, size_t offset, int whence)
{
  // printf("set %d = %d\n", fd, offset);
  if(whence == 0)
  {
    file_table[fd].open_offset = offset;
  }
  else if(whence == 1)
  {
    file_table[fd].open_offset += offset;
  }
  else if(whence == 2)
  {
    file_table[fd].open_offset = file_table[fd].size + offset;
  }
  else assert(0);
  return file_table[fd].open_offset;
}

int fs_close(int fd)
{
  return 0;
}

void init_fs() 
{
  file_table[FD_FB].size = (io_read(AM_GPU_CONFIG).width)*(io_read(AM_GPU_CONFIG).height);
}