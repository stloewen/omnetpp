//==========================================================================
//  ONSTARTUP.H - part of
//                     OMNeT++/OMNEST
//            Discrete System Simulation in C++
//
//
//  EXECUTE_ON_STARTUP macro and supporting class
//
//==========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 1992-2005 Andras Varga

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `license' for details on this and other legal matters.
*--------------------------------------------------------------*/

#ifndef __ONSTARTUP_H
#define __ONSTARTUP_H

#include <vector>
#include <map>
#include "simkerneldefs.h"
#include "cownedobject.h"

NAMESPACE_BEGIN


// Generating identifiers unique for this file. See MSVC Help for __COUNTER__
// for more info.
#define __CONCAT1(x,y) x##y
#define __CONCAT2(prefix,line) __CONCAT1(prefix,line)
#define MAKE_UNIQUE_WITHIN_FILE(prefix) __CONCAT2(prefix,__LINE__)

// helpers for EXECUTE_ON_STARTUP
// IMPORTANT: if you change "__onstartup_func_" below, linkall.pl must also be updated!
#define __ONSTARTUP_FUNC  MAKE_UNIQUE_WITHIN_FILE(__onstartup_func_)
#define __ONSTARTUP_OBJ   MAKE_UNIQUE_WITHIN_FILE(__onstartup_obj_)

// helper
#define __FILEUNIQUENAME__  MAKE_UNIQUE_WITHIN_FILE(__uniquename_)

/**
 * Allows code fragments to be collected in global scope which will
 * then be executed from main() right after program startup. This is
 * used by in \opp for building global registration lists of
 * module types, network types, etc. Registration lists in fact
 * are a simple substitute for Java's Class.forName() method...
 *
 * @hideinitializer
 */
#define EXECUTE_ON_STARTUP(CODE)  \
  namespace { \
    void __ONSTARTUP_FUNC() {CODE;} \
    static ExecuteOnStartup __ONSTARTUP_OBJ(__ONSTARTUP_FUNC); \
  };


/**
 * Supporting class for EXECUTE_ON_STARTUP macro.
 *
 * @ingroup Internals
 */
class SIM_API ExecuteOnStartup
{
  private:
    void (*code_to_exec)();
    ExecuteOnStartup *next;
    static ExecuteOnStartup *head;
  public:
    ExecuteOnStartup(void (*code_to_exec)());
    ~ExecuteOnStartup();
    void execute();
    static void executeAll();
};


/**
 * Stores objects with a qualified name: namespace + name. Namespaces may be
 * nested, the namespace element separator is "::". The name() method of
 * objects should return the unqualified name (without namespace, e.g. "Queue"),
 * and the fullName() method the qualified name (with namespace, e.g. "INET::Queue").
 */
//XXX better name
//XXX why are those objects cOwnedObjects? cNamedObject not enough?
class SIM_API cSymTable : public cNoncopyableOwnedObject
{
  private:
    std::vector<cOwnedObject *> v;  // stores the objects
    typedef std::map<std::string, cOwnedObject*> LookupCache;
    LookupCache lookupCache;  // to remember lookups
  public:
    cSymTable(const char *name) : cNoncopyableOwnedObject(name) {}
    virtual ~cSymTable();

    /** @name cObject methods */
    //@{
    virtual std::string info() const;
    virtual void forEachChild(cVisitor *v);
    //@}

    /**
     * Adds an object to the container.
     */
    virtual void add(cOwnedObject *obj);

    /**
     * Returns the number of elements.
     */
    virtual int size();

    /**
     * Returns the ith element.
     */
    virtual cOwnedObject *get(int i);

    /**
     * Searches for the given object name (not fullName!), and returns
     * the first object one found.
     */
    virtual cOwnedObject *get(const char *name);

    /**
     * Sorts the elements by qualified name (fullName()). This affects
     * the order get() will return the elements.
     */
    virtual void sort();

    /**
     * Returns the object with exactly the given qualified name (fullName()).
     */
    virtual cOwnedObject *lookup(const char *qualifiedName);

    /**
     * Performs lookup of the qualified name. Lookup starts with the context
     * namespace, then it keeps discarding the last component of the context
     * namespace and try to find the name in that namespace.
     */
    virtual cOwnedObject *lookup(const char *name, const char *contextNamespace);
};

/**
 * Singleton class, used for global registration lists.
 *
 * @ingroup Internals
 */
class SIM_API cRegistrationList
{
  private:
    cSymTable *inst;
    const char *tmpname;
  public:
    cRegistrationList();
    cRegistrationList(const char *name);
    ~cRegistrationList();
    cSymTable *instance();
    void clear();
};

NAMESPACE_END


#endif

