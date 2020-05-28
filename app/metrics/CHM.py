# Implementation of metrics proposed by FoSCI
# You can find the original at https://github.com/jinwuxia/RS17_project_program


import sys
import csv

global g_clusterID2Interf2APIDict  # [clusterID][interface] = list[api id ...]
global g_apiDict  # [api id] = api object

#api is operation


class APIObject:
    def __init__(self, clusterID, interface, apiName, parameterSet, returnSet):
        self.clusterID = clusterID
        self.interface = interface
        self.apiName = apiName
        self.parameterSet = parameterSet
        self.returnSet = returnSet


def Trans2Set(strstr):
    if strstr == '':
        resList = ['void']
    else:
        resList = strstr.split(',')
    return set(resList)


def GetInterf(api):
    # interface name
    apiList = api.split('.')
    del apiList[len(apiList) - 1]
    interface = '.'.join(apiList)
    return interface


def ReadAPIFile(fileName):
    apiID = 0
    clusterID2Interf2ApiDict = dict()
    apiDict = dict()

    with open(fileName, 'r', newline="") as fp:
        reader = csv.reader(fp)
        for each in reader:
            [clusterID, interfName, apiName, parameterstr, returnstr] = each
            if each[0] == 'clusterID':
                continue
            clusterID = int(clusterID)
            parameterSet = Trans2Set(parameterstr)
            returnSet = Trans2Set(returnstr)
            interface = GetInterf(apiName)
            if clusterID not in clusterID2Interf2ApiDict:
                clusterID2Interf2ApiDict[clusterID] = dict()
            if interface not in clusterID2Interf2ApiDict[clusterID]:
                clusterID2Interf2ApiDict[clusterID][interface] = list()
            oneObejct = APIObject(clusterID, interface,
                                  apiName, parameterSet, returnSet)
            apiDict[apiID] = oneObejct
            clusterID2Interf2ApiDict[clusterID][interface].append(apiID)
            apiID += 1
    # print clusterID2Interf2ApiDict
    # print apiDict
    return clusterID2Interf2ApiDict, apiDict


def GetIntersect(apiID1, apiID2):
    global g_apiDict
    para_interset = g_apiDict[apiID1].parameterSet & g_apiDict[apiID2].parameterSet
    return_interset = g_apiDict[apiID1].returnSet & g_apiDict[apiID2].returnSet
    return para_interset, return_interset


def GetUnionset(apiID1, apiID2):
    global g_apiDict
    para_unionset = g_apiDict[apiID1].parameterSet | g_apiDict[apiID2].parameterSet
    return_unionset = g_apiDict[apiID1].returnSet | g_apiDict[apiID2].returnSet
    return para_unionset, return_unionset


# compute the edge between two apis
# if have common para/return type, then have an edge between the two operations/apis
def GetEdge_half(interSet, unionSet):
    edge_unwei = 0
    edge_wei = 0
    if len(unionSet) == 0:
        return -1, -1
    if len(interSet) != 0:
        edge_unwei = 1
        edge_wei = len(interSet) / float(len(unionSet))
    # print edge_unwei, edge_wei
    return edge_unwei, edge_wei

# measure the meg-level 's interface cohesion'


def Metric_msg_cohesion(clusterID):
    global g_clusterID2Interf2APIDict
    apiIDList = getAllAPIForCluster(clusterID)
    if len(apiIDList) == 1:
        cohesion_wei = 1
    else:
        from itertools import combinations
        apiIDPairList = list(combinations(apiIDList, 2))
        fenmu = len(apiIDPairList)  # perfect graph's edge number
        apiSimList = list()
        for apiPair in apiIDPairList:
            [para_interset, return_interset] = GetIntersect(
                apiPair[0], apiPair[1])
            [para_unionset, return_unionset] = GetUnionset(
                apiPair[0], apiPair[1])

            [para_unweight, para_weight] = GetEdge_half(
                para_interset, para_unionset)
            [return_unweight, return_weight] = GetEdge_half(
                return_interset, return_unionset)
            # FEITO
            if para_weight != -1 and return_weight != -1:
                sim = (para_weight + return_weight) / float(2.0)
                apiSimList.append(sim)

            elif para_weight == -1 and return_weight != -1:
                sim = return_weight
                apiSimList.append(sim)
            elif para_weight != -1 and return_weight == -1:
                sim = para_weight
                apiSimList.append(sim)
        cohesion_wei = sum(apiSimList) / float(len(apiSimList))
    return cohesion_wei

# get api list for a cluster
# g_clusterID2Interf2APIDict[clusterID][interface] = [api id ....]


def getAllAPIForCluster(clusterID):
    apiIDList = list()
    global g_clusterID2Interf2APIDict
    for interface in g_clusterID2Interf2APIDict[clusterID]:
        apis = g_clusterID2Interf2APIDict[clusterID][interface]
        apiIDList.extend(apis)
    return apiIDList


def calculate(apiFileName):
    global g_clusterID2Interf2APIDict  # [clusterID][interface] = [api id ....]
    global g_apiDict

    [g_clusterID2Interf2APIDict, g_apiDict] = ReadAPIFile(apiFileName)
    msg_cohesion_wei_list = list()
    if len(g_clusterID2Interf2APIDict) == 0:
        print(str(1))
        return 1
    else:
        for clusterID in g_clusterID2Interf2APIDict:
            msg_cohesion_wei = Metric_msg_cohesion(clusterID)
            #print ('cluster,' + str(clusterID) + ',' + str(msg_cohesion_wei) )
            msg_cohesion_wei_list.append(msg_cohesion_wei)
        msg_avg_wei = sum(msg_cohesion_wei_list) / \
            float(len(msg_cohesion_wei_list))

        interface_number = 0
        for clusterID in g_clusterID2Interf2APIDict:
            interface_number += len(g_clusterID2Interf2APIDict[clusterID])
        #print ('interface number=', interface_number)
        # tmp = ['avg_msg_cohesion', str(msg_avg_wei), 'interface_numb', str(
        #     interface_number), 'clusterHasinf', str(len(g_clusterID2Interf2APIDict))]
        # print(','.join(tmp))

        # print apidetail, using do and m_cohesion_wei_list interface_number_list
        #print('\ninterface msg cohesion detail:')
        # for index in range(0, len(msg_cohesion_wei_list)):
        #    print(msg_cohesion_wei_list[index])
        print(f"CHM: {msg_avg_wei}")
        return msg_avg_wei


if __name__ == '__main__':
    apiFileName = sys.argv[1]
    calculate(apiFileName)
