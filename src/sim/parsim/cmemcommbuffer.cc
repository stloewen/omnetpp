//=========================================================================
//  CMEMCOMMBUFFER.CC - part of
//
//                  OMNeT++/OMNEST
//           Discrete System Simulation in C++
//
//   Written by:  Andras Varga, 2003
//
//=========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 2003-2005 Andras Varga
  Monash University, Dept. of Electrical and Computer Systems Eng.
  Melbourne, Australia

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `license' for details on this and other legal matters.
*--------------------------------------------------------------*/

#include <string.h>
#include "cmemcommbuffer.h"
#include "util.h"  // opp_string
#include "cobject.h"
#include "macros.h"

Register_Class(cMemCommBuffer);


#define STORE(type,d)    {*(type *)(mBuffer+mMsgSize)=d; mMsgSize+=sizeof(type);}
#define EXTRACT(type,d)  {d=*(type *)(mBuffer+mPosition); mPosition+=sizeof(type);}

#define STOREARRAY(type,d,size)   {memcpy(mBuffer+mMsgSize,d,size*sizeof(type)); mMsgSize+=size*sizeof(type);}
#define EXTRACTARRAY(type,d,size) {memcpy(d,mBuffer+mPosition,size*sizeof(type)); mPosition+=size*sizeof(type);}

cMemCommBuffer::cMemCommBuffer()
{
}

cMemCommBuffer::~cMemCommBuffer ()
{
}


void cMemCommBuffer::pack(char d)
{
    extendBufferFor(sizeof(char));
    STORE(char,d);
}


void cMemCommBuffer::pack(unsigned char d)
{
    extendBufferFor(sizeof(unsigned char));
    STORE(unsigned char,d);
}


void cMemCommBuffer::pack(bool d)
{
    extendBufferFor(sizeof(bool));
    STORE(bool,d);
}


void cMemCommBuffer::pack(short d)
{
    extendBufferFor(sizeof(short));
    STORE(short,d);
}


void cMemCommBuffer::pack(unsigned short d)
{
    extendBufferFor(sizeof(unsigned short));
    STORE(unsigned short,d);
}


void cMemCommBuffer::pack(int d)
{
    extendBufferFor(sizeof(int));
    STORE(int,d);
}


void cMemCommBuffer::pack(unsigned int d)
{
    extendBufferFor(sizeof(unsigned int));
    STORE(unsigned int,d);
}


void cMemCommBuffer::pack(long d)
{
    extendBufferFor(sizeof(long));
    STORE(long,d);
}


void cMemCommBuffer::pack(unsigned long d)
{
    extendBufferFor(sizeof(unsigned long));
    STORE(unsigned long,d);
}


void cMemCommBuffer::pack(float d)
{
    extendBufferFor(sizeof(float));
    STORE(float,d);
}


void cMemCommBuffer::pack(double d)
{
    extendBufferFor(sizeof(double));
    STORE(double,d);
}


void cMemCommBuffer::pack(long double d)
{
    extendBufferFor(sizeof(long double));
    STORE(long double,d);
}


// pack a string
void cMemCommBuffer::pack(const char *d)
{
    int len = d ? strlen(d) : 0;
    pack(len);
    extendBufferFor(len*sizeof(char));
    STOREARRAY(char,d,len);
}

void cMemCommBuffer::pack(opp_string& d)
{
    pack(d.buffer());
}


void cMemCommBuffer::pack(const char *d, int size)
{
    extendBufferFor(size*sizeof(char));
    STOREARRAY(char,d,size);
}


void cMemCommBuffer::pack(unsigned char *d, int size)
{
    extendBufferFor(size*sizeof(unsigned char));
    STOREARRAY(unsigned char,d,size);
}


void cMemCommBuffer::pack(bool *d, int size)
{
    extendBufferFor(size*sizeof(bool));
    STOREARRAY(bool,d,size);
}


void cMemCommBuffer::pack(short *d, int size)
{
    extendBufferFor(size*sizeof(short));
    STOREARRAY(short,d,size);
}


void cMemCommBuffer::pack(unsigned short *d, int size)
{
    extendBufferFor(size*sizeof(unsigned short));
    STOREARRAY(unsigned short,d,size);
}


void cMemCommBuffer::pack(int *d, int size)
{
    extendBufferFor(size*sizeof(int));
    STOREARRAY(int,d,size);
}


void cMemCommBuffer::pack(unsigned int *d, int size)
{
    extendBufferFor(size*sizeof(unsigned int));
    STOREARRAY(unsigned int,d,size);
}


void cMemCommBuffer::pack(long *d, int size)
{
    extendBufferFor(size*sizeof(long));
    STOREARRAY(long,d,size);
}


void cMemCommBuffer::pack(unsigned long *d, int size)
{
    extendBufferFor(size*sizeof(unsigned long));
    STOREARRAY(unsigned long,d,size);
}


void cMemCommBuffer::pack(float *d, int size)
{
    extendBufferFor(size*sizeof(float));
    STOREARRAY(float,d,size);
}


void cMemCommBuffer::pack(double *d, int size)
{
    extendBufferFor(size*sizeof(double));
    STOREARRAY(double,d,size);
}


void cMemCommBuffer::pack(long double *d, int size)
{
    extendBufferFor(size*sizeof(long double));
    STOREARRAY(long double,d,size);
}


// pack string array
void cMemCommBuffer::pack(const char **d, int size)
{
    for (int i = 0; i < size; i++)
        pack(d[i]);
}

void cMemCommBuffer::pack(opp_string *d, int size)
{
    for (int i = 0; i < size; i++)
        pack(d[i]);
}

//--------------------------------

void cMemCommBuffer::unpack(char& d)
{
    EXTRACT(char,d);
}


void cMemCommBuffer::unpack(unsigned char& d)
{
    EXTRACT(unsigned char,d);
}

void cMemCommBuffer::unpack(bool& d)
{
    EXTRACT(bool,d);
}

void cMemCommBuffer::unpack(short& d)
{
    EXTRACT(short,d);
}


void cMemCommBuffer::unpack(unsigned short& d)
{
    EXTRACT(unsigned short,d);
}


void cMemCommBuffer::unpack(int& d)
{
    EXTRACT(int,d);
}


void cMemCommBuffer::unpack(unsigned int& d)
{
    EXTRACT(unsigned int,d);
}


void cMemCommBuffer::unpack(long& d)
{
    EXTRACT(long,d);
}


void cMemCommBuffer::unpack(unsigned long& d)
{
    EXTRACT(unsigned long,d);
}


void cMemCommBuffer::unpack(float& d)
{
    EXTRACT(float,d);
}


void cMemCommBuffer::unpack(double& d)
{
    EXTRACT(double,d);
}


void cMemCommBuffer::unpack(long double& d)
{
    EXTRACT(long double,d);
}


// unpack a string
void cMemCommBuffer::unpack(const char *&d)
{
    int len;
    unpack(len);
    char *tmp = new char[len+1];
    EXTRACTARRAY(char,tmp,len);
    tmp[len] = '\0';
    d = tmp;
}

void cMemCommBuffer::unpack(opp_string& d)
{
    int len;
    unpack(len);
    d.reserve(len+1);
    EXTRACTARRAY(char,d.buffer(),len);
    d.buffer()[len] = '\0';
}

void cMemCommBuffer::unpack(char *d, int size)
{
    EXTRACTARRAY(char,d,size);
}


void cMemCommBuffer::unpack(unsigned char *d, int size)
{
    EXTRACTARRAY(unsigned char,d,size);
}


void cMemCommBuffer::unpack(bool *d, int size)
{
    EXTRACTARRAY(bool,d,size);
}

void cMemCommBuffer::unpack(short *d, int size)
{
    EXTRACTARRAY(short,d,size);
}


void cMemCommBuffer::unpack(unsigned short *d, int size)
{
    EXTRACTARRAY(unsigned short,d,size);
}


void cMemCommBuffer::unpack(int *d, int size)
{
    EXTRACTARRAY(int,d,size);
}


void cMemCommBuffer::unpack(unsigned int *d, int size)
{
    EXTRACTARRAY(unsigned,d,size);
}


void cMemCommBuffer::unpack(long *d, int size)
{
    EXTRACTARRAY(long,d,size);
}


void cMemCommBuffer::unpack(unsigned long *d, int size)
{
    EXTRACTARRAY(unsigned long,d,size);
}


void cMemCommBuffer::unpack(float *d, int size)
{
    EXTRACTARRAY(float,d,size);
}


void cMemCommBuffer::unpack(double *d, int size)
{
    EXTRACTARRAY(double,d,size);
}


void cMemCommBuffer::unpack(long double *d, int size)
{
    EXTRACTARRAY(long double,d,size);
}

void cMemCommBuffer::unpack(const char **d, int size)
{
    for (int i = 0; i < size; i++)
        unpack(d[i]);
}

void cMemCommBuffer::unpack(opp_string *d, int size)
{
    for (int i = 0; i < size; i++)
        unpack(d[i]);
}

