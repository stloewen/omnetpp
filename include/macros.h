//==========================================================================
//   MACROS.H  - header for
//                             OMNeT++
//            Discrete System Simulation in C++
//
//  Definition of the following macros:
//    Define_Network, Define_Link
//    ModuleInterface..End
//    Define_Module
//    Module_Class_Members
//    Define_Function
//    Register_Class
//
//==========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 1992-2003 Andras Varga

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `license' for details on this and other legal matters.
*--------------------------------------------------------------*/

#ifndef __MACROS_H
#define __MACROS_H

#include "onstartup.h"
#include "ctypes.h"

//=========================================================================

/**
 * @name Declaration macros
 * @ingroup Macros
 */
//@{

/**
 * Network declaration macro. It can be found in code generated by the
 * NEDC compiler. The use of this macro allows the creation of a network
 * when only its name is available as a string. (Typically, the name of the
 * network to be simulated is read from the configuration file.)
 * The macro expands to the definition of a cNetworkType object.
 *
 * @hideinitializer
 */
#define Define_Network(NAME) \
  EXECUTE_ON_STARTUP(NAME##__net, (new NAME(#NAME))->setOwner(&networks);)

/**
 * Link type definition. The macro expands to the definition of a cLinkType object;
 * the last three arguments are pointers to functions which dynamically create cPar
 * objects an return their pointers.
 *
 * @hideinitializer
 */
#define Define_Link(NAME,DELAY,ERROR,DATARATE) \
  EXECUTE_ON_STARTUP(NAME##__linkt, (new cLinkType(#NAME, DELAY, ERROR, DATARATE))->setOwner(&linktypes);)

/**
 * Registers a mathematical function that takes 0, 1, 2 or 3 double arguments
 * and returns a double. The use of this macro allows the function to be used
 * in expressions inside NED network descriptions.
 *
 * Commonly used <math.h> functions have Define_Function() lines in the OMNeT++
 * simulation kernel.
 *
 * @hideinitializer
 */
#define Define_Function(NAME,ARGCOUNT) \
  EXECUTE_ON_STARTUP(NAME##__##ARGCOUNT##__func, (new cFunctionType(#NAME,NAME,ARGCOUNT))->setOwner(&functions);)

/**
 * Like Define_Function(), but takes three arguments, the second one being the
 * pointer to the function. This macro allows registering a function with a
 * different name than its implementation.
 *
 * @hideinitializer
 */
#define Define_Function2(NAME,FUNCTION,ARGCOUNT) \
  EXECUTE_ON_STARTUP(NAME##__##ARGCOUNT##__func, (new cFunctionType(#NAME,FUNCTION,ARGCOUNT))->setOwner(&functions);)

/**
 * Register class. This defines a factory object which makes it possible
 * to create an object by the passing class name to the createOne() function.
 *
 * @hideinitializer
 */
#define Register_Class(CLASSNAME) \
  void *CLASSNAME##__create() {return new CLASSNAME;} \
  EXECUTE_ON_STARTUP(CLASSNAME##__class, (new cClassRegister(#CLASSNAME,CLASSNAME##__create))->setOwner(&classes);)

//@}

//=========================================================================

/**
 * @name Module declaration macros
 * @ingroup Macros
 */
//@{

/**
 * Announces the class as a module to OMNeT++ and couples it with the
 * NED interface of the same name. The macro expands to the definition
 * a cModuleType object.
 *
 * The NEDC compiler generates Define_Module() lines for all compound modules.
 * However, it is the user's responsibility to put Define_Module() lines for
 * all simple module types into one of the C++ sources.
 *
 * @hideinitializer
 */
#define Define_Module(CLASSNAME) \
  static cModule *CLASSNAME##__create(const char *name, cModule *parentmod) \
  { \
     return (cModule *) new CLASSNAME(name, parentmod); \
  } \
  EXECUTE_ON_STARTUP(CLASSNAME##__mod, (new cModuleType(#CLASSNAME,#CLASSNAME,(ModuleCreateFunc)CLASSNAME##__create))->setOwner(&modtypes);)

/**
 * Similar to Define_Module(), except that it couples the class with the
 * NED interface of the given name. This macro is used in connection with
 * the 'like' phrase in NED.
 *
 * @hideinitializer
 */
#define Define_Module_Like(CLASSNAME,INTERFACENAME) \
  static cModule *CLASSNAME##__create(const char *name, cModule *parentmod) \
  { \
     return (cModule *) new CLASSNAME(name, parentmod); \
  } \
  EXECUTE_ON_STARTUP(CLASSNAME##__mod, (new cModuleType(#CLASSNAME,#INTERFACENAME,(ModuleCreateFunc)CLASSNAME##__create))->setOwner(&modtypes);)

/**
 * This macro facilitates the declaration of a simple module class, and
 * it expands to the definition of mandatory member functions.
 * (Currently only a constructor.)
 *
 * The macro is used like this:
 *
 * <PRE>
 *  class CLASSNAME : public cSimpleModule
 *  {
 *     Module_Class_Members(CLASSNAME,cSimpleModule,8192)
 *     virtual void activity();
 *  };
 * </PRE>
 *
 * @hideinitializer
 */
#define Module_Class_Members(CLASSNAME,BASECLASS,STACK) \
    public: \
      CLASSNAME(const char *name, cModule *parentmod, unsigned stk=STACK) : \
           BASECLASS(name, parentmod, stk) {}
//@}

//=========================================================================

// internal: declaration of a module interface (module gates and params)
// example:
//    ModuleInterface(CLASSNAME)
//        Gate(NAME,TYPE)
//        Parameter(NAME,TYPES)
//        Machine(NAME)
//    EndInterface
//
#define ModuleInterface(CLASSNAME)    static cModuleInterface::sDescrItem CLASSNAME##__descr[] = {
#define Gate(NAME,TYPE)         {'G', #NAME, NULL,  TYPE},
#define Parameter(NAME,TYPES)   {'P', #NAME, TYPES, 0   },
#define Machine(NAME)           {'M', #NAME, NULL,  0   },
#define EndInterface            {'E', NULL,  NULL,  0   }};

// internal: registers a module interface specified with the Interface..EndInterface macros
#define Register_ModuleInterface(CLASSNAME) \
  EXECUTE_ON_STARTUP(CLASSNAME##__if, (new cModuleInterface(#CLASSNAME, CLASSNAME##__descr))->setOwner(&modinterfaces);)

// internal: gate types. To be used with module interface declarations.
#define GateDir_Input      'I'
#define GateDir_Output     'O'

// internal: parameter allowed types. To be used with module interface declarations.
#define ParType_Const      "#"
#define ParType_Any        "*"
#define ParType_Numeric    "LDXFT"
#define ParType_Bool       "B"
#define ParType_String     "S"

#endif

