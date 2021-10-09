clear all
clc
close all
% analog input to A0
comPort='COM7';
s=serial(comPort);
set(s,'DataBits',8);
set(s,'StopBits',1);
set(s,'BaudRate',1200);
set(s,'Parity','none');
fopen(s)
a=1;

for l=1:10
    a=1;
    clear sig
c1=clock;
c1=fix(c1)    
while(a<=100)
    temp=str2num(fscanf(s));
 if temp>=0
    sig(a)=temp/1024*5;
    a=a+1;
 end
end
c2=clock;
c2=fix(c2)
tp=c2-c1;
tp=tp(5)*60+tp(6);
plot((1:length(sig))*tp/100,sig)
xlabel(['Time period = ' num2str(tp) 'seconds'])
pause(0.01);
end

fclose(s);
delete(s)
disp('Done')