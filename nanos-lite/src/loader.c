#include <proc.h>
#include <elf.h>
#include "fs.h"

#ifdef __LP64__
# define Elf_Ehdr Elf64_Ehdr
# define Elf_Phdr Elf64_Phdr
#else
# define Elf_Ehdr Elf32_Ehdr
# define Elf_Phdr Elf32_Phdr
#endif

static Elf64_Ehdr elf_header;
static Elf64_Phdr elf_program;

extern size_t ramdisk_read(void *buf, size_t offset, size_t len);
extern size_t get_ramdisk_size();

static uintptr_t loader(PCB *pcb, const char *filename) 
{
  /*以下为使用ramdisk加载navy-app程序*/
  // // elf_header = *(Elf64_Ehdr *)(&ramdisk_start); //读取elf头
  // ramdisk_read(&elf_header, 0, sizeof(Elf64_Ehdr));
  // assert(*(uint32_t *)(elf_header.e_ident) == 0x464c457f);
  // assert(elf_header.e_type == 0x02);

  // Elf64_Half psize = elf_header.e_phentsize;
  // Elf64_Half pnum = elf_header.e_phnum;

  // for(int i=0;i<pnum;i++)
  // {
  //   // elf_program = *(Elf64_Phdr *)(&ramdisk_start + elf_header.e_phoff + i*psize); //读取当前的程序头表, 用这句会导致difftest不过，不知道什么原因
  //   // memcpy(&elf_program, &ramdisk_start + elf_header.e_phoff + i*psize, psize);
  //   ramdisk_read(&elf_program, elf_header.e_phoff + i*psize, sizeof(Elf64_Phdr)); //读取当前的程序头表
  //   if(elf_program.p_type == PT_LOAD)
  //   {
  //     // printf("offset:0x%x\t", elf_program.p_offset);
  //     // printf("vaddr:0x%x\t", elf_program.p_vaddr);
  //     // printf("filesize:0x%x\t", elf_program.p_filesz);
  //     // printf("memsize:0x%x\t\n", elf_program.p_memsz);
  //     ramdisk_read((void *)(elf_program.p_vaddr), elf_program.p_offset, elf_program.p_filesz);
  //     if(elf_program.p_filesz != elf_program.p_memsz) //需要将bss段清零
  //     {
  //       memset((void *)(elf_program.p_vaddr+elf_program.p_filesz), 0, (elf_program.p_memsz-elf_program.p_filesz));
  //     }
  //   }
  // }


  int fd = fs_open(filename, 0, 0);
  fs_read(fd, &elf_header, sizeof(Elf64_Ehdr));

  assert(*(uint32_t *)(elf_header.e_ident) == 0x464c457f);
  assert(elf_header.e_type == 0x02);

  Elf64_Half psize = elf_header.e_phentsize;
  Elf64_Half pnum = elf_header.e_phnum;

  for(int i=0;i<pnum;i++)
  {
    fs_lseek(fd, elf_header.e_phoff+i*psize, 0);
    fs_read(fd, &elf_program, sizeof(Elf64_Phdr)); //读取当前的程序头表
    if(elf_program.p_type == PT_LOAD)
    {
      // printf("offset:0x%x\t", elf_program.p_offset);
      // printf("vaddr:0x%x\t", elf_program.p_vaddr);
      // printf("filesize:0x%x\t", elf_program.p_filesz);
      // printf("memsize:0x%x\t\n", elf_program.p_memsz);
      // ramdisk_read((void *)(elf_program.p_vaddr), elf_program.p_offset, elf_program.p_filesz);
      fs_lseek(fd, elf_program.p_offset, 0);
      fs_read(fd, (void *)(elf_program.p_vaddr), elf_program.p_filesz);
      if(elf_program.p_filesz != elf_program.p_memsz) //需要将bss段清零
      {
        memset((void *)(elf_program.p_vaddr+elf_program.p_filesz), 0, (elf_program.p_memsz-elf_program.p_filesz));
      }
    }
  }
  return (uintptr_t)(elf_header.e_entry);
}

void naive_uload(PCB *pcb, const char *filename) 
{
  uintptr_t entry = loader(pcb, filename);
  Log("Jump to entry = 0x%x", entry);
  ((void(*)())entry) ();
}
