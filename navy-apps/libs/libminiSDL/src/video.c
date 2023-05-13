#include <NDL.h>
#include <sdl-video.h>
#include <assert.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

/*typedef struct {
	uint32_t flags;
	SDL_PixelFormat *format;
	int w, h;
	uint16_t pitch;
	uint8_t *pixels;
} SDL_Surface;*/


void SDL_BlitSurface(SDL_Surface *src, SDL_Rect *srcrect, SDL_Surface *dst, SDL_Rect *dstrect)
{
  assert(dst && src);
  assert(dst->format->BitsPerPixel == src->format->BitsPerPixel);
  // printf("This is SDL_BlitSurface.\n");
  // printf("This is the end of SDL_BlitSurface.\n");
  int srcrect_w = 0;
  int srcrect_h = 0;
  int srcrect_x = 0;
  int srcrect_y = 0;
  int dstrect_x = 0;
  int dstrect_y = 0;

  if(srcrect == NULL)
  {
    srcrect_w = src->w;
    srcrect_h = src->h;
    srcrect_x = 0;
    srcrect_y = 0;
  }
  else
  {
    srcrect_w = srcrect->w;
    srcrect_h = srcrect->h;
    srcrect_x = srcrect->x;
    srcrect_y = srcrect->y;
  }

  if(dstrect == NULL)
  {
    dstrect_x = 0;
    dstrect_y = 0;
  }
  else
  {
    dstrect_x = dstrect->x;
    dstrect_y = dstrect->y; 
  }

  // printf("src Surface:w:%d h:%d bitsperpixel:%d\n", src->w, src->h, src->format->BitsPerPixel);
  // printf("src rect:w:%d h:%d x:%d y:%D\n", srcrect_w, srcrect_h, srcrect_x, srcrect_y);
  // printf("dst rect:x:%d y:%D\n", dstrect_x, dstrect_y);
  // if(dst->w !=0 && dst->h != 0)printf("dst Surface:w:%d h:%d bitsperpixel:%d\n", dst->w, dst->h, dst->format->BitsPerPixel);
  if(dst->format->BitsPerPixel == 32)
  {
    for(int row = 0; row < srcrect_h; row++)
    {
      for(int col = 0; col < srcrect_w; col++)
      {
        ((uint32_t *)(dst->pixels))[dst->w*(row+dstrect_y)+dstrect_x+col] = ((uint32_t *)(src->pixels))[src->w*(row+srcrect_y)+col+srcrect_x];
      }
    }
  }
  else if(dst->format->BitsPerPixel == 8)
  {
    for(int row = 0; row < srcrect_h; row++)
    {
      for(int col = 0; col < srcrect_w; col++)
      {
        ((uint8_t *)(dst->pixels))[dst->w*(row+dstrect_y)+dstrect_x+col] = ((uint8_t *)(src->pixels))[src->w*(row+srcrect_y)+col+srcrect_x];
      }
    }
  }
  else
  {
    printf("BitsPerPixel=%d unhandled\n", dst->format->BitsPerPixel);
    assert(0);
  }
  return;
}


void SDL_FillRect(SDL_Surface *dst, SDL_Rect *dstrect, uint32_t color)
{
  // printf("This is SDL_FillRect.\n");

  int draw_w, draw_h, draw_x, draw_y;
  if(dstrect != NULL)
  {
    draw_w = dstrect->w;
    draw_h = dstrect->h;
    draw_x = dstrect->x;
    draw_y = dstrect->y;
  }
  else
  {
    draw_w = dst->w;
    draw_h = dst->h;
    draw_x = 0;
    draw_y = 0;
  }
  // printf("%d %d %d %d\n", draw_w, draw_h, draw_x, draw_y);

  for(int row=0;row < draw_h;row++)
  {
    for(int col=0;col < draw_w;col++)
    {
      ((uint32_t*)(dst->pixels))[(draw_y + row)*dst->w + draw_x + col] = color;
    }
  }
  NDL_DrawRect((uint32_t *)(dst->pixels), draw_x, draw_y, draw_w, draw_h);
  // printf("This is the end of SDL_FillRect.\n");
}

void SDL_UpdateRect(SDL_Surface *s, int x, int y, int w, int h)
{
  // printf("This is SDL_UpdateRect.\n");
  int real_w = w;
  int real_h = h;
  if(w == 0 && h == 0 && x == 0 && y == 0)
  {
    real_w = s->w;
    real_h = s->h;
  }
  uint32_t *frame = malloc(sizeof(uint32_t)*real_w*real_h);
  assert(frame);
  // printf("BitsPerpixel:%d\n", s->format->BitsPerPixel); // 在nslide中是32bits
  if(s->format->BitsPerPixel == 32) // 在nslide中是32bits
  {
    NDL_DrawRect((uint32_t*)s->pixels, x, y, real_w, real_h);
  }
  else
  {
    for(int row=0;row < real_h;row++)
    {
      for(int col=0;col < real_w;col++)
      {
        frame[row*real_w+col] = s->format->palette->colors[s->pixels[(row+y)*(s->w)+col+x]].r;
        frame[row*real_w+col] <<= 8;
        frame[row*real_w+col] |= s->format->palette->colors[s->pixels[(row+y)*(s->w)+col+x]].g;
        frame[row*real_w+col] <<= 8;
        frame[row*real_w+col] |= s->format->palette->colors[s->pixels[(row+y)*(s->w)+col+x]].b;
      }
    }
    NDL_DrawRect(frame, x, y, real_w, real_h);
    // printf("SDL_UpdateRect has encountered a bad situation.\n");
    // assert(0);
  }
  free(frame);
  // printf("This is the end of SDL_UpdateRect.\n");
}

// APIs below are already implemented.

static inline int maskToShift(uint32_t mask) {
  switch (mask) {
    case 0x000000ff: return 0;
    case 0x0000ff00: return 8;
    case 0x00ff0000: return 16;
    case 0xff000000: return 24;
    case 0x00000000: return 24; // hack
    default: assert(0);
  }
}

SDL_Surface* SDL_CreateRGBSurface(uint32_t flags, int width, int height, int depth,
    uint32_t Rmask, uint32_t Gmask, uint32_t Bmask, uint32_t Amask) {
  assert(depth == 8 || depth == 32);
  SDL_Surface *s = malloc(sizeof(SDL_Surface));
  assert(s);
  s->flags = flags;
  s->format = malloc(sizeof(SDL_PixelFormat));
  assert(s->format);
  if (depth == 8) {
    s->format->palette = malloc(sizeof(SDL_Palette));
    assert(s->format->palette);
    s->format->palette->colors = malloc(sizeof(SDL_Color) * 256);
    assert(s->format->palette->colors);
    memset(s->format->palette->colors, 0, sizeof(SDL_Color) * 256);
    s->format->palette->ncolors = 256;
  } else {
    s->format->palette = NULL;
    s->format->Rmask = Rmask; s->format->Rshift = maskToShift(Rmask); s->format->Rloss = 0;
    s->format->Gmask = Gmask; s->format->Gshift = maskToShift(Gmask); s->format->Gloss = 0;
    s->format->Bmask = Bmask; s->format->Bshift = maskToShift(Bmask); s->format->Bloss = 0;
    s->format->Amask = Amask; s->format->Ashift = maskToShift(Amask); s->format->Aloss = 0;
  }

  s->format->BitsPerPixel = depth;
  s->format->BytesPerPixel = depth / 8;

  s->w = width;
  s->h = height;
  s->pitch = width * depth / 8;
  assert(s->pitch == width * s->format->BytesPerPixel);

  if (!(flags & SDL_PREALLOC)) {
    s->pixels = malloc(s->pitch * height);
    assert(s->pixels);
  }

  return s;
}

SDL_Surface* SDL_CreateRGBSurfaceFrom(void *pixels, int width, int height, int depth,
    int pitch, uint32_t Rmask, uint32_t Gmask, uint32_t Bmask, uint32_t Amask) {
  SDL_Surface *s = SDL_CreateRGBSurface(SDL_PREALLOC, width, height, depth,
      Rmask, Gmask, Bmask, Amask);
  assert(pitch == s->pitch);
  s->pixels = pixels;
  return s;
}

void SDL_FreeSurface(SDL_Surface *s) {
  if (s != NULL) {
    if (s->format != NULL) {
      if (s->format->palette != NULL) {
        if (s->format->palette->colors != NULL) free(s->format->palette->colors);
        free(s->format->palette);
      }
      free(s->format);
    }
    if (s->pixels != NULL && !(s->flags & SDL_PREALLOC)) free(s->pixels);
    free(s);
  }
}

SDL_Surface* SDL_SetVideoMode(int width, int height, int bpp, uint32_t flags)
{
  if (flags & SDL_HWSURFACE)
  {
    NDL_OpenCanvas(&width, &height);
    printf("After NDL_OpenCanvas, w:%d h:%d\n", width, height);
  }
  return SDL_CreateRGBSurface(flags, width, height, bpp,
      DEFAULT_RMASK, DEFAULT_GMASK, DEFAULT_BMASK, DEFAULT_AMASK);
}

void SDL_SoftStretch(SDL_Surface *src, SDL_Rect *srcrect, SDL_Surface *dst, SDL_Rect *dstrect) {
  assert(src && dst);
  assert(dst->format->BitsPerPixel == src->format->BitsPerPixel);
  assert(dst->format->BitsPerPixel == 8);

  int x = (srcrect == NULL ? 0 : srcrect->x);
  int y = (srcrect == NULL ? 0 : srcrect->y);
  int w = (srcrect == NULL ? src->w : srcrect->w);
  int h = (srcrect == NULL ? src->h : srcrect->h);

  assert(dstrect);
  if(w == dstrect->w && h == dstrect->h) {
    /* The source rectangle and the destination rectangle
     * are of the same size. If that is the case, there
     * is no need to stretch, just copy. */
    SDL_Rect rect;
    rect.x = x;
    rect.y = y;
    rect.w = w;
    rect.h = h;
    SDL_BlitSurface(src, &rect, dst, dstrect);
  }
  else {
    assert(0);
  }
}

void SDL_SetPalette(SDL_Surface *s, int flags, SDL_Color *colors, int firstcolor, int ncolors) {
  assert(s);
  assert(s->format);
  assert(s->format->palette);
  assert(firstcolor == 0);

  s->format->palette->ncolors = ncolors;
  memcpy(s->format->palette->colors, colors, sizeof(SDL_Color) * ncolors);

  if(s->flags & SDL_HWSURFACE) {
    assert(ncolors == 256);
    for (int i = 0; i < ncolors; i ++) {
      uint8_t r = colors[i].r;
      uint8_t g = colors[i].g;
      uint8_t b = colors[i].b;
    }
    SDL_UpdateRect(s, 0, 0, 0, 0);
  }
}

static void ConvertPixelsARGB_ABGR(void *dst, void *src, int len) {
  int i;
  uint8_t (*pdst)[4] = dst;
  uint8_t (*psrc)[4] = src;
  union {
    uint8_t val8[4];
    uint32_t val32;
  } tmp;
  int first = len & ~0xf;
  for (i = 0; i < first; i += 16) {
#define macro(i) \
    tmp.val32 = *((uint32_t *)psrc[i]); \
    *((uint32_t *)pdst[i]) = tmp.val32; \
    pdst[i][0] = tmp.val8[2]; \
    pdst[i][2] = tmp.val8[0];

    macro(i + 0); macro(i + 1); macro(i + 2); macro(i + 3);
    macro(i + 4); macro(i + 5); macro(i + 6); macro(i + 7);
    macro(i + 8); macro(i + 9); macro(i +10); macro(i +11);
    macro(i +12); macro(i +13); macro(i +14); macro(i +15);
  }

  for (; i < len; i ++) {
    macro(i);
  }
}

SDL_Surface *SDL_ConvertSurface(SDL_Surface *src, SDL_PixelFormat *fmt, uint32_t flags) {
  assert(src->format->BitsPerPixel == 32);
  assert(src->w * src->format->BytesPerPixel == src->pitch);
  assert(src->format->BitsPerPixel == fmt->BitsPerPixel);

  SDL_Surface* ret = SDL_CreateRGBSurface(flags, src->w, src->h, fmt->BitsPerPixel,
    fmt->Rmask, fmt->Gmask, fmt->Bmask, fmt->Amask);

  assert(fmt->Gmask == src->format->Gmask);
  assert(fmt->Amask == 0 || src->format->Amask == 0 || (fmt->Amask == src->format->Amask));
  ConvertPixelsARGB_ABGR(ret->pixels, src->pixels, src->w * src->h);

  return ret;
}

uint32_t SDL_MapRGBA(SDL_PixelFormat *fmt, uint8_t r, uint8_t g, uint8_t b, uint8_t a) {
  assert(fmt->BytesPerPixel == 4);
  uint32_t p = (r << fmt->Rshift) | (g << fmt->Gshift) | (b << fmt->Bshift);
  if (fmt->Amask) p |= (a << fmt->Ashift);
  return p;
}

int SDL_LockSurface(SDL_Surface *s)
{
  return 0;
}

void SDL_UnlockSurface(SDL_Surface *s)
{
}


// #define SDL_BYTESPERPIXEL(format) ((format)->BytesPerPixel)
// void SDL_BlitSurface(SDL_Surface *src, SDL_Rect *srcrect, SDL_Surface *dst, SDL_Rect *dstrect)
// {
//   printf("This is SDL_BlitSurface.\n");
//     int src_x, src_y, dst_x, dst_y;
//     int copy_width, copy_height;
//     int src_pitch, dst_pitch;
//     uint8_t *src_pixels, *dst_pixels;
//     int pixel_size;

//     // Calculate the source and destination coordinates and dimensions
//     if (srcrect) {
//         src_x = srcrect->x;
//         src_y = srcrect->y;
//         copy_width = srcrect->w;
//         copy_height = srcrect->h;
//     } else {
//         src_x = 0;
//         src_y = 0;
//         copy_width = src->w;
//         copy_height = src->h;
//     }
//     if (dstrect) {
//         dst_x = dstrect->x;
//         dst_y = dstrect->y;
//     } else {
//         dst_x = 0;
//         dst_y = 0;
//     }

//     // Get the pixel format and size
//     pixel_size = SDL_BYTESPERPIXEL(src->format);

//     // Get the source and destination pixel pointers and pitches
//     src_pixels = (uint8_t *)src->pixels + src_y * src->pitch + src_x * pixel_size;
//     dst_pixels = (uint8_t *)dst->pixels + dst_y * dst->pitch + dst_x * pixel_size;
//     src_pitch = src->pitch;
//     dst_pitch = dst->pitch;

//     // Copy the pixels
//     for (int y = 0; y < copy_height; y++) {
//         uint8_t *src_row = src_pixels + y * src_pitch;
//         uint8_t *dst_row = dst_pixels + y * dst_pitch;
//         for (int x = 0; x < copy_width; x++) {
//             uint8_t *src_pixel = src_row + x * pixel_size;
//             uint8_t *dst_pixel = dst_row + x * pixel_size;
//             for (int i = 0; i < pixel_size; i++) {
//                 dst_pixel[i] = src_pixel[i];
//             }
//         }
//     }
// }