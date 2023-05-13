#include <NDL.h>
#include <string.h>
#include <SDL.h>

#define keyname(k) #k,
static uint8_t key_state[128] = {0};

static const char *keyname[] = {
  "NONE",
  _KEYS(keyname)
};

int SDL_PushEvent(SDL_Event *ev)
{
  return 0;
}

int SDL_PollEvent(SDL_Event *ev)
{
  char buf[128];
  char buf2[128];
  if(NDL_PollEvent(buf, sizeof(buf)) == 0) return 0;
  else
  {
    memcpy(buf2, buf+3,strlen(buf)-2);
    int cur = 0;
    int keycode = 0;
    int len = sizeof(keyname);
    for(int i = 0;i < 83; i++)
    {
      if(strcmp(keyname[i],buf2) == 0)
      {
        keycode = i;
        cur = len;
      }
      cur += sizeof(keyname[i]);
    }

    if(buf[0] == 'k' && buf[1] == 'u')
    {
      ev->type = SDL_KEYUP;
      key_state[keycode] = 0;
    }else if(buf[0] == 'k' && buf[1] == 'd')
    {
      ev->type = SDL_KEYDOWN;
      key_state[keycode] = 1;
    }
    ev->key.keysym.sym = keycode;
    return 1;
  }
  return 0;
}

/*typedef struct {
  uint8_t sym;
} SDL_keysym;

typedef struct {
  uint8_t type;
  SDL_keysym keysym;
} SDL_KeyboardEvent;

typedef struct {
  uint8_t type;
  int code;
  void *data1;
  void *data2;
} SDL_UserEvent;

typedef union {
  uint8_t type;
  SDL_KeyboardEvent key;
  SDL_UserEvent user;
} SDL_Event;*/


int SDL_WaitEvent(SDL_Event *event) 
{
  // printf("This is SDL_WaitEvent.\n");
  // printf("key dect:%s\n", (char *)buf);

  char buf[128];
  char buf2[128];
  int event_flag = NDL_PollEvent(buf,sizeof(buf));
  while(event_flag == 0)
  {
    event_flag = NDL_PollEvent(buf,sizeof(buf));
  }

  memcpy(buf2, buf+3,strlen(buf)-2);
  int cur = 0;
  int keycode = 0;
  int len = sizeof(keyname);
  for(int i = 0;i < 83; i++)
  {
    if(strcmp(keyname[i],buf2) == 0)
    {
      keycode = i;
      cur = len;
    }
    cur += sizeof(keyname[i]);
  }

  if(buf[0] == 'k' && buf[1] == 'u')
  {
    event->type = SDL_KEYUP;
    key_state[keycode] = 0;
  }else if(buf[0] == 'k' && buf[1] == 'd')
  {
    event->type = SDL_KEYDOWN;
    key_state[keycode] = 1;
  }
  event->key.keysym.sym = keycode;
  return 1;

}

int SDL_PeepEvents(SDL_Event *ev, int numevents, int action, uint32_t mask)
{
  return 0;
}

uint8_t* SDL_GetKeyState(int *numkeys)
{
  return key_state;
}
