from django.http import HttpResponse
import mysql.connector


def oximeter(request):
    try:
        name = request.GET['a']
        bmp = request.GET['b']
        spo2 = request.GET['c']
        pi = request.GET['d']
        print(name)
        print(bmp)
        print(spo2)
        print(pi)
        print('\n')

        con = mysql.connector.connect(user='root', password='root', host='localhost')
        cursor = con.cursor()
        sql = "insert into clm.oximeter_data(name,bmp,spo2,pi) values('" + name + "','" + bmp + "','" + spo2 + "','" + pi + "');"
        cursor.execute(sql)
        con.commit()
        return HttpResponse('OK')
    except Exception:
        return HttpResponse(Exception)


def thermometer(request):
    try:
        name = request.GET['a']
        temperature = request.GET['b']
        print(name)
        print(temperature)

        con = mysql.connector.connect(user='root', password='root', host='localhost')
        cursor = con.cursor()
        sql = "insert into clm.thermometer_data(name,temperature) values('" + name + "','" + temperature + "');"
        cursor.execute(sql)
        con.commit()
        return HttpResponse('OK')
    except Exception as e:
        print(e)
        return HttpResponse(e)


def acquire_oximeter(request):
    try:
        name = request.GET['name']
        begin_year = request.GET['begin_year']
        begin_month = request.GET['begin_month']
        begin_day = request.GET['begin_day']
        end_year = request.GET['end_year']
        end_month = request.GET['end_month']
        end_day = request.GET['end_day']
        print(name)
        print(begin_year)
        print(begin_month)
        print(begin_day)
        print(end_year)
        print(end_month)
        print(end_day)
        con = mysql.connector.connect(user='root', password='root', host='localhost')
        cursor = con.cursor()
        sql = "select time,bmp,spo2,pi from clm.oximeter_data where Date(time) between '" + begin_year + "-" + begin_month + "-" + begin_day + "' and  '" + end_year + "-" + end_month + "-" + end_day + "';"
        print(sql)
        cursor.execute(sql)
        result = cursor.fetchall()
        con.commit()
        cursor.close()

        str = ""
        for i in result:
            str += i[0].strftime('%H%M%S') + "@" + i[1] + "@" + i[2] + "@" + i[3] + "#"
        print(str)
        return HttpResponse(str)
    except Exception as e:
        print(e)
        return HttpResponse(e)


def acquire_thermometer(request):
    try:
        name = request.GET['name']
        begin_year = request.GET['begin_year']
        begin_month = request.GET['begin_month']
        begin_day = request.GET['begin_day']
        end_year = request.GET['end_year']
        end_month = request.GET['end_month']
        end_day = request.GET['end_day']
        print(name)
        print(begin_year)
        print(begin_month)
        print(begin_day)
        print(end_year)
        print(end_month)
        print(end_day)
        con = mysql.connector.connect(user='root', password='root', host='localhost')
        cursor = con.cursor()
        sql = "select time,temperature from clm.thermometer_data where Date(time) between '" + begin_year + "-" + begin_month + "-" + begin_day + "' and  '" + end_year + "-" + end_month + "-" + end_day + "';"
        print(sql)
        cursor.execute(sql)
        result = cursor.fetchall()
        con.commit()
        cursor.close()

        str = ""
        for i in result:
            str += i[0].strftime('%H%M%S') + "@" + i[1] + "#"
        print(str)
        return HttpResponse(str)
    except Exception as e:
        print(e)
        return HttpResponse(e)


def test():
    con = mysql.connector.connect(user='root', password='root', host='localhost')
    cursor = con.cursor()
    sql = "truncate clm.oximeter_data;"
    cursor.execute(sql)
    con.commit()
#     str = ""
#
#     con = mysql.connector.connect(user='root', password='root', host='localhost')
#     cursor = con.cursor()
#     sql = "select time,bmp,spo2,pi from clm.data where mac='A4:C1:38:BE:57:AA';"
#     cursor.execute(sql)
#     result = cursor.fetchall()
#     con.commit()
#     cursor.close()
#
#     for i in result:
#         time = i[0].split(' ')[0] + i[0].split(' ')[1] + i[0].split(' ')[2] + i[0].split(' ')[3] + i[0].split(' ')[4] + i[0].split(' ')[5]
#         str += time + "@" + i[1] + "@" + i[2] + "@" + i[3] + "#"
#     print(str)


# mac = "A4:C1:38:BE:57:AA"
# con = mysql.connector.connect(user='root', password='root', host='localhost')
# cursor = con.cursor()
# sql = "select time,bmp,spo2,pi from clm.data where mac='" + mac + "' and time > '2019 02 24 14 42 00' and time < '2019 02 24 14 42 19';"
# cursor.execute(sql)
# result = cursor.fetchall()
# con.commit()
# cursor.close()
# print(result)
# test()
#
# bmp = "120"
# spo2 = "100"
# pi = "10"
#

# con = mysql.connector.connect(user='root', password='root', host='localhost')
# cursor = con.cursor()
# sql = "select time,bmp,spo2,pi from clm.oximeter_data where Date(time) between '2019-03-13' and  '2019-03-14';"
# print(sql)
# cursor.execute(sql)
# result = cursor.fetchall()
# con.commit()
# cursor.close()
#
# str = ""
# for i in result:
#     str += i[0].strftime('%H%M%S') + "@" + i[1] + "@" + i[2] + "@" + i[3] + "#"
# print(str)

