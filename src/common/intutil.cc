//==========================================================================
//   INTUTIL.CC  - part of
//                     OMNeT++/OMNEST
//            Discrete System Simulation in C++
//
//  Author: Andras Varga
//
//==========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 1992-2017 Andras Varga
  Copyright (C) 2006-2017 OpenSim Ltd.

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `license' for details on this and other legal matters.
*--------------------------------------------------------------*/

#include <cmath>
#include <limits>
#include <cinttypes>
#include "intutil.h"
#include "commonutil.h"
#include "exception.h"

namespace omnetpp {
namespace common {

#ifndef __has_builtin
  #define __has_builtin(x) 0  // Compatibility for compilers without the __has_builtin macro
#endif

void intCastError(const std::string& num, const char *errmsg)
{
    throw opp_runtime_error(errmsg ? errmsg : "Overflow casting %s to the target integer type", num.c_str());
}

intpar_t safeSub(intpar_t a, intpar_t b)
{
#if (__has_builtin(__builtin_sub_overflow) && !defined(__c2__)) || (defined(__GNUC__) && !defined(__clang__) && __GNUC__ >= 5)
    intpar_t res;
    if (__builtin_sub_overflow(a, b, &res))
        throw opp_runtime_error("Integer overflow subtracting %" PRId64 " from %" PRId64 ", try casting operands to double", (int64_t)b, (int64_t)a);
    return res;
#else
    return a - b; // unchecked
#endif
}

intpar_t safeAdd(intpar_t a, intpar_t b)
{
#if (__has_builtin(__builtin_add_overflow) && !defined(__c2__)) || (defined(__GNUC__) && !defined(__clang__) && __GNUC__ >= 5)
    intpar_t res;
    if (__builtin_add_overflow(a, b, &res))
        throw opp_runtime_error("Integer overflow adding %" PRId64 " and %" PRId64 ", try casting operands to double", (int64_t)a, (int64_t)b);
    return res;
#else
    return a + b;  // unchecked
#endif
}

intpar_t safeMul(intpar_t a, intpar_t b)
{
#if (__has_builtin(__builtin_mul_overflow) && !defined(__c2__)) || (defined(__GNUC__) && !defined(__clang__) && __GNUC__ >= 5)
    intpar_t res;
    if (__builtin_mul_overflow(a, b, &res))
        throw opp_runtime_error("Integer overflow multiplying %" PRId64 " and %" PRId64 ", try casting operands to double", (int64_t)a, (int64_t)b);
    return res;
#else
    const intpar_t int32max = std::numeric_limits<int32_t>::max();
    if ((a & ~int32max) == 0 && (b & ~int32max) == 0)
        return a * b;
    intpar_t res = a * b;
    if (res / a != b)
        throw opp_runtime_error("Integer overflow multiplying %" PRId64 " and %" PRId64 ", try casting operands to double", (int64_t)a, (int64_t)b);
    return res;
#endif
}

intpar_t intPow(intpar_t base, intpar_t exp)
{
    Assert(exp >= 0);
    if (exp == 0)
        return 1;
    try {
        intpar_t result = 1;
        while (true) {
            if (exp & 1)
                result = safeMul(result, base);
            exp >>= 1;
            if (exp == 0)
                break;
            base = safeMul(base, base);
        }
        return result;
    }
    catch (const opp_runtime_error& e) {
        throw opp_runtime_error("Overflow during integer exponentiation, try casting operands to double");
    }
}

intpar_t shift(intpar_t a, intpar_t b)
{
    // positive b = left shift, negative b = (arithmetic) right shift
    // note: result of ">>" and "<<" is undefined if shift is larger or equal to the width of the integer
    const int width = 8*sizeof(a);
    if (b > 0)
        return b < width ? (a << b) : 0;
    else
        return -width < b ? (a >> -b) : a > 0 ? 0 : ~(intpar_t)0;
}

}  // namespace common
}  // namespace omnetpp

